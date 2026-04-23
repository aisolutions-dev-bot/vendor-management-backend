package com.aisolutions.vendormanagement.service.email;

import com.aisolutions.vendormanagement.dto.VendorInvSubmissionDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Builds the HTML email body for invoice approval notification.
 */
public class InvoiceEmailTemplate {

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

  public static String build(
      VendorInvSubmissionDTO invoice,
      String recipientName,
      String approveUrl,
      String rejectUrl) {

    return "<!DOCTYPE html>" +
        "<html lang='en'>" +
        "<head>" +
        "<meta charset='UTF-8'>" +
        "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
        "<title>Invoice Approval Request</title>" +
        "</head>" +
        "<body style='margin:0;padding:0;background:#f4f6f9;font-family:-apple-system,BlinkMacSystemFont,\"Segoe UI\",Roboto,sans-serif;'>" +
        "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f4f6f9;padding:40px 0;'>" +
        "<tr><td align='center'>" +
        "<table width='600' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);'>" +

        // Header
        "<tr><td style='background:#1e293b;padding:32px 40px;'>" +
        "<h1 style='margin:0;color:#ffffff;font-size:20px;font-weight:600;'>Invoice Approval Request</h1>" +
        "<p style='margin:6px 0 0;color:#94a3b8;font-size:14px;'>Action required — please review and respond</p>" +
        "</td></tr>" +

        // Greeting
        "<tr><td style='padding:32px 40px 0;'>" +
        "<p style='margin:0;color:#1e293b;font-size:15px;'>Hi <strong>" + escHtml(recipientName) + "</strong>,</p>" +
        "<p style='margin:12px 0 0;color:#475569;font-size:14px;line-height:1.6;'>" +
        "A new invoice has been submitted and requires your approval. Please review the details below." +
        "</p>" +
        "</td></tr>" +

        // Invoice details card
        "<tr><td style='padding:24px 40px;'>" +
        "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f8fafc;border-radius:8px;border:1px solid #e2e8f0;overflow:hidden;'>" +
        "<tr><td style='padding:16px 20px;border-bottom:1px solid #e2e8f0;background:#f1f5f9;'>" +
        "<span style='font-size:13px;font-weight:700;color:#64748b;text-transform:uppercase;letter-spacing:0.05em;'>Invoice Details</span>" +
        "</td></tr>" +
        detailRow("Invoice Number", invoice.getInvoiceNumber()) +
        detailRow("Vendor", invoice.getVendorName()) +
        detailRow("Invoice Date", formatDate(invoice.getInvoiceDate())) +
        detailRow("Due Date", formatDate(invoice.getInvoiceDueDate())) +
        detailRow("PO Reference", invoice.getReferenceOur()) +
        detailRow("Project Code", invoice.getProjectCode()) +
        detailRow("Currency", invoice.getCurrency()) +
        detailRow("Total Amount", formatAmount(invoice.getTotalForeign(), invoice.getCurrency())) +
        "</table>" +
        "</td></tr>" +

        // Expiry notice
        "<tr><td style='padding:0 40px;'>" +
        "<p style='margin:0;color:#f59e0b;font-size:13px;background:#fffbeb;border:1px solid #fde68a;border-radius:6px;padding:10px 14px;'>" +
        "⚠️ These action links expire in <strong>48 hours</strong> and can only be used once." +
        "</p>" +
        "</td></tr>" +

        // CTA buttons
        "<tr><td style='padding:32px 40px;'>" +
        "<table cellpadding='0' cellspacing='0'>" +
        "<tr>" +
        "<td style='padding-right:12px;'>" +
        "<a href='" + approveUrl + "' style='display:inline-block;background:#10b981;color:#ffffff;text-decoration:none;" +
        "padding:14px 28px;border-radius:8px;font-size:15px;font-weight:600;letter-spacing:0.01em;'>✓ Approve Invoice</a>" +
        "</td>" +
        "<td>" +
        "<a href='" + rejectUrl + "' style='display:inline-block;background:#ffffff;color:#ef4444;text-decoration:none;" +
        "padding:14px 28px;border-radius:8px;font-size:15px;font-weight:600;border:2px solid #ef4444;letter-spacing:0.01em;'>✕ Reject Invoice</a>" +
        "</td>" +
        "</tr>" +
        "</table>" +
        "</td></tr>" +

        // Footer
        "<tr><td style='padding:24px 40px;border-top:1px solid #e2e8f0;'>" +
        "<p style='margin:0;color:#94a3b8;font-size:12px;line-height:1.6;'>" +
        "This email was sent automatically by the AI Solutions vendor management system.<br>" +
        "Do not reply to this email. If you have questions, please contact your system administrator." +
        "</p>" +
        "</td></tr>" +

        "</table>" +
        "</td></tr>" +
        "</table>" +
        "</body></html>";
  }

  // #region Helpers

  private static String detailRow(String label, String value) {
    String safeValue = (value != null && !value.isBlank()) ? escHtml(value) : "<span style='color:#94a3b8;'>—</span>";
    return "<tr>" +
        "<td style='padding:12px 20px;border-bottom:1px solid #e2e8f0;width:40%;'>" +
        "<span style='font-size:13px;color:#64748b;font-weight:500;'>" + escHtml(label) + "</span>" +
        "</td>" +
        "<td style='padding:12px 20px;border-bottom:1px solid #e2e8f0;'>" +
        "<span style='font-size:13px;color:#1e293b;font-weight:600;'>" + safeValue + "</span>" +
        "</td>" +
        "</tr>";
  }

  private static String formatDate(LocalDateTime dt) {
    if (dt == null) return null;
    return dt.format(DATE_FMT);
  }

  private static String formatAmount(BigDecimal amount, String currency) {
    if (amount == null) return null;
    String cur = (currency != null) ? currency : "MYR";
    return cur + " " + String.format("%,.2f", amount);
  }

  private static String escHtml(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }

  // #endregion
}
