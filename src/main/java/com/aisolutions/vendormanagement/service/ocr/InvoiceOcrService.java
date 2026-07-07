package com.aisolutions.vendormanagement.service.ocr;

import com.aisolutions.vendormanagement.client.OpenAIClient;
import com.aisolutions.vendormanagement.client.OpenAIClient.*;
import com.aisolutions.vendormanagement.dto.InvoiceOcrResultDTO;
import com.aisolutions.vendormanagement.dto.InvoiceOcrResultDTO.LineItem;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Extracts vendor invoice data from an uploaded image using OpenAI GPT-4o Vision.
 *
 * The model reads the invoice layout semantically, so no per-vendor keyword rules
 * are needed even though each vendor's invoice format differs. Returns header fields
 * + line items, which the frontend pre-fills into the editable Invoice Details
 * section for the vendor to confirm before saving.
 */
@ApplicationScoped
public class InvoiceOcrService {

    private static final Logger LOG = Logger.getLogger(InvoiceOcrService.class);

    @Inject
    @RestClient
    OpenAIClient openAIClient;

    private final ObjectMapper mapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT =
        "You are an invoice data extraction assistant. " +
        "Extract structured data from commercial/sales invoice images with high precision. " +
        "Always respond with valid JSON only — no markdown, no explanation.";

    private static final String USER_PROMPT =
        "Extract the following fields from this vendor invoice image.\n" +
        "Header fields:\n" +
        "- vendorInvoiceNumber: the vendor's own invoice number\n" +
        "- vendorName: the vendor/supplier company name issuing the invoice\n" +
        "- invoiceDate: invoice date in yyyy-MM-dd format\n" +
        "- currency: currency code e.g. SGD, MYR, USD\n" +
        "- termsDay: payment terms in number of days if stated (integer, else null)\n" +
        "- subTotal: total amount before tax (numeric, no currency symbol)\n" +
        "- tax: total tax/GST amount (numeric, 0 if none)\n" +
        "- total: grand total including tax (numeric)\n" +
        "Line items (one object per invoice line):\n" +
        "- description: item or service description\n" +
        "- quantity: quantity (numeric, null if not shown)\n" +
        "- unitPrice: unit price (numeric, null if not shown)\n" +
        "- amount: line amount before tax (numeric)\n" +
        "- taxAmount: line tax amount (numeric, 0 if none)\n" +
        "- total: line total including tax (numeric)\n" +
        "\n" +
        "Respond ONLY with this JSON structure (use null for fields not found):\n" +
        "{\n" +
        "  \"vendorInvoiceNumber\": \"...\",\n" +
        "  \"vendorName\": \"...\",\n" +
        "  \"invoiceDate\": \"...\",\n" +
        "  \"currency\": \"...\",\n" +
        "  \"termsDay\": null,\n" +
        "  \"subTotal\": 0.00,\n" +
        "  \"tax\": 0.00,\n" +
        "  \"total\": 0.00,\n" +
        "  \"lineItems\": [\n" +
        "    { \"description\": \"...\", \"quantity\": 0, \"unitPrice\": 0.00, \"amount\": 0.00, \"taxAmount\": 0.00, \"total\": 0.00 }\n" +
        "  ]\n" +
        "}";

    /**
     * Extracts invoice header + line items from an uploaded invoice file.
     *
     * @param fileBytes  raw bytes of the invoice (PDF, JPG, PNG, etc.)
     * @param mimeType   content type e.g. "application/pdf", "image/jpeg", "image/png"
     * @return           extracted invoice fields
     */
    public Uni<InvoiceOcrResultDTO> extractFromFile(byte[] fileBytes, String mimeType) {
        if (fileBytes == null || fileBytes.length == 0) {
            LOG.warn("[InvoiceOcr] Empty file bytes received");
            InvoiceOcrResultDTO err = new InvoiceOcrResultDTO();
            err.setSuccess(false);
            err.setErrorMessage("Invoice file is empty or missing");
            return Uni.createFrom().item(err);
        }

        // OpenAI vision only accepts images, so a PDF invoice is rendered to page
        // images (PDFBox) before being sent; image uploads pass straight through.
        List<ContentPart> imageParts;
        try {
            imageParts = buildImageParts(fileBytes, mimeType);
        } catch (Exception e) {
            LOG.error("[InvoiceOcr] Failed to prepare invoice file: " + e.getMessage(), e);
            InvoiceOcrResultDTO err = new InvoiceOcrResultDTO();
            err.setSuccess(false);
            err.setErrorMessage("Could not read the invoice file: " + e.getMessage());
            return Uni.createFrom().item(err);
        }

        List<ContentPart> userContent = new ArrayList<>();
        userContent.add(ContentPart.text(USER_PROMPT));
        userContent.addAll(imageParts);

        var systemMsg = new TextMessage("system", SYSTEM_PROMPT);
        var userMsg = new VisionMessage("user", userContent);

        OpenAIRequest request = new OpenAIRequest(List.of(systemMsg, userMsg));
        request.model = "gpt-4o-mini";
        request.temperature = 0.1;
        request.max_tokens = 2048; // invoices carry line-item tables, so allow a larger response

        LOG.info("[InvoiceOcr] Sending " + imageParts.size() + " image(s) to OpenAI (" + fileBytes.length + " source bytes)");

        return openAIClient.chat(request)
            .map(response -> {
                String content = response.getContent();
                LOG.info("[InvoiceOcr] OpenAI response: " + content);
                return parseResponse(content);
            })
            .onFailure().recoverWithItem(err -> {
                LOG.error("[InvoiceOcr] OpenAI call failed: " + err.getMessage(), err);
                InvoiceOcrResultDTO result = new InvoiceOcrResultDTO();
                result.setSuccess(false);
                result.setErrorMessage("AI extraction failed: " + err.getMessage());
                return result;
            });
    }

    // ─────────────────────────────────────────────────────────────────────
    // FILE → IMAGE PARTS
    // ─────────────────────────────────────────────────────────────────────

    private static final int MAX_PDF_PAGES = 5;   // guard against huge multi-page PDFs
    private static final float PDF_RENDER_DPI = 150f;

    /**
     * Builds the vision image parts from the uploaded file. A PDF is rendered
     * page-by-page to PNG images (OpenAI vision can't read PDFs directly); an
     * image is passed through as-is.
     */
    private List<ContentPart> buildImageParts(byte[] fileBytes, String mimeType) throws IOException {
        if (isPdf(fileBytes, mimeType)) {
            List<ContentPart> parts = new ArrayList<>();
            try (PDDocument doc = PDDocument.load(fileBytes)) {
                PDFRenderer renderer = new PDFRenderer(doc);
                int pages = Math.min(doc.getNumberOfPages(), MAX_PDF_PAGES);
                LOG.info("[InvoiceOcr] Rendering " + pages + " of " + doc.getNumberOfPages() + " PDF page(s) to image");
                for (int i = 0; i < pages; i++) {
                    BufferedImage img = renderer.renderImageWithDPI(i, PDF_RENDER_DPI, ImageType.RGB);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(img, "png", baos);
                    parts.add(ContentPart.image(Base64.getEncoder().encodeToString(baos.toByteArray()), "image/png"));
                }
            }
            if (parts.isEmpty()) {
                throw new IOException("PDF has no renderable pages");
            }
            return parts;
        }
        String mime = (mimeType != null && !mimeType.isBlank()) ? mimeType : "image/jpeg";
        return List.of(ContentPart.image(Base64.getEncoder().encodeToString(fileBytes), mime));
    }

    private boolean isPdf(byte[] bytes, String mimeType) {
        if (mimeType != null && mimeType.toLowerCase().contains("pdf")) return true;
        // Fallback to the %PDF magic header in case the content type is missing/generic
        return bytes.length >= 5
            && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F';
    }

    // ─────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private InvoiceOcrResultDTO parseResponse(String json) {
        InvoiceOcrResultDTO result = new InvoiceOcrResultDTO();

        if (json == null || json.isBlank()) {
            result.setSuccess(false);
            result.setErrorMessage("Empty response from AI");
            return result;
        }

        try {
            // Strip markdown code fences if present
            String cleaned = json.trim();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("```[a-z]*\\n?", "").replace("```", "").trim();
            }

            Map<String, Object> parsed = mapper.readValue(cleaned, Map.class);

            result.setVendorInvoiceNumber(getString(parsed, "vendorInvoiceNumber"));
            result.setVendorName(getString(parsed, "vendorName"));
            result.setInvoiceDate(getString(parsed, "invoiceDate"));
            result.setCurrency(getString(parsed, "currency"));
            result.setTermsDay(getInteger(parsed, "termsDay"));
            result.setSubTotal(getDecimal(parsed, "subTotal"));
            result.setTax(getDecimal(parsed, "tax"));
            result.setTotal(getDecimal(parsed, "total"));

            Object linesRaw = parsed.get("lineItems");
            if (linesRaw instanceof List<?> lines) {
                List<LineItem> items = new ArrayList<>();
                for (Object o : lines) {
                    if (o instanceof Map<?, ?> m) {
                        Map<String, Object> lm = (Map<String, Object>) m;
                        LineItem item = new LineItem();
                        item.setDescription(getString(lm, "description"));
                        item.setQuantity(getDecimal(lm, "quantity"));
                        item.setUnitPrice(getDecimal(lm, "unitPrice"));
                        item.setAmount(getDecimal(lm, "amount"));
                        item.setTaxAmount(getDecimal(lm, "taxAmount"));
                        item.setTotal(getDecimal(lm, "total"));
                        items.add(item);
                    }
                }
                result.setLineItems(items);
            }

            result.setSuccess(true);
        } catch (Exception e) {
            LOG.error("[InvoiceOcr] Failed to parse JSON response: " + e.getMessage());
            result.setSuccess(false);
            result.setErrorMessage("Failed to parse AI response: " + e.getMessage());
        }

        return result;
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null || "null".equals(val.toString())) return null;
        String s = val.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private BigDecimal getDecimal(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null || "null".equals(val.toString())) return null;
        try {
            return new BigDecimal(val.toString().trim());
        } catch (Exception e) {
            LOG.warn("[InvoiceOcr] Could not parse decimal for '" + key + "': " + val);
            return null;
        }
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null || "null".equals(val.toString())) return null;
        try {
            return new BigDecimal(val.toString().trim()).intValue();
        } catch (Exception e) {
            LOG.warn("[InvoiceOcr] Could not parse integer for '" + key + "': " + val);
            return null;
        }
    }
}
