package com.aisolutions.vendormanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Result returned from the OpenAI vision extraction of a vendor's uploaded
 * commercial/sales invoice. All fields are nullable — the frontend pre-fills the
 * editable Invoice Details section with these values and the vendor confirms/corrects
 * before saving. Header fields map to m02VendInvSubmission, line items to
 * m02VendInvSubmissionDet.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceOcrResultDTO {

    /** Vendor's own invoice number (maps to VendorInvoice) */
    private String vendorInvoiceNumber;

    /** Vendor / supplier name as printed on the invoice */
    private String vendorName;

    /** Invoice date in ISO format yyyy-MM-dd */
    private String invoiceDate;

    /** Currency code e.g. SGD, MYR, USD */
    private String currency;

    /** Payment terms in days if stated on the invoice */
    private Integer termsDay;

    /** Sum of line amounts before tax (maps to SubTotalForeign) */
    private BigDecimal subTotal;

    /** Total tax / GST amount (maps to TaxForeign) */
    private BigDecimal tax;

    /** Grand total incl. tax (maps to TotalForeign) */
    private BigDecimal total;

    /** Extracted line items */
    private List<LineItem> lineItems;

    /** True if extraction succeeded */
    private boolean success = true;

    /** Error message if extraction failed */
    private String errorMessage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineItem {
        /** Item / service description */
        private String description;

        /** Quantity */
        private BigDecimal quantity;

        /** Unit price */
        private BigDecimal unitPrice;

        /** Line amount before tax (maps to detail SubTotalForeign) */
        private BigDecimal amount;

        /** Line tax amount (maps to detail TaxForeign) */
        private BigDecimal taxAmount;

        /** Line total incl. tax (maps to detail TotalForeign) */
        private BigDecimal total;
    }
}
