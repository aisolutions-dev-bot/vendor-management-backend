package com.aisolutions.vendormanagement.service.email;

import com.aisolutions.vendormanagement.dto.VendorInvSubmissionDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Builds the HTML email body for invoice approval notification.
 * Design: Violet/Slate theme matching AI Solutions design system.
 */
public class InvoiceEmailTemplate {

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

  // Colours matching the app's Aura/Violet/Slate theme
  private static final String COLOR_PRIMARY      = "#7c3aed";
  private static final String COLOR_PRIMARY_DARK = "#6d28d9";
  private static final String COLOR_PRIMARY_SOFT = "#ede9fe";
  private static final String COLOR_REJECT       = "#dc2626";
  private static final String COLOR_REJECT_SOFT  = "#fee2e2";
  private static final String COLOR_BG           = "#f8fafc";
  private static final String COLOR_CARD         = "#ffffff";
  private static final String COLOR_BORDER       = "#e2e8f0";
  private static final String COLOR_TEXT         = "#0f172a";
  private static final String COLOR_TEXT_MUTED   = "#64748b";
  private static final String COLOR_TEXT_LIGHT   = "#94a3b8";

  public static String build(
      VendorInvSubmissionDTO invoice,
      String recipientName,
      String approveUrl,
      String rejectUrl) {

    return "<!DOCTYPE html>" +
        "<html lang='en'>" +
        "<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1.0'>" +
        "<title>Invoice Approval Request</title></head>" +
        "<body style='margin:0;padding:0;background:" + COLOR_BG + ";font-family:-apple-system,BlinkMacSystemFont,\"Segoe UI\",Roboto,\"Helvetica Neue\",sans-serif;'>" +
        "<table width='100%' cellpadding='0' cellspacing='0' style='background:" + COLOR_BG + ";padding:40px 16px;'>" +
        "<tr><td align='center'>" +
        "<table width='600' cellpadding='0' cellspacing='0' style='max-width:600px;width:100%;background:" + COLOR_CARD + ";border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.07);border:1px solid " + COLOR_BORDER + ";'>" +

        // ── Header bar ──
        "<tr><td style='background:" + COLOR_PRIMARY + ";padding:28px 40px;'>" +
        "<table width='100%' cellpadding='0' cellspacing='0'><tr>" +
        "<td>" +
        "<p style='margin:0;font-size:11px;font-weight:600;letter-spacing:0.08em;text-transform:uppercase;color:#c4b5fd;'>AI Solutions</p>" +
        "<h1 style='margin:6px 0 0;color:#ffffff;font-size:20px;font-weight:700;letter-spacing:-0.01em;'>Invoice Approval Request</h1>" +
        "</td>" +
        "<td align='right'>" +
        "<div style='background:rgba(255,255,255,0.15);border-radius:8px;padding:8px 14px;display:inline-block;'>" +
        "<span style='color:#ffffff;font-size:13px;font-weight:600;'>" + escHtml(invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "—") + "</span>" +
        "</div>" +
        "</td></tr></table>" +
        "</td></tr>" +

        // ── Greeting ──
        "<tr><td style='padding:32px 40px 0;'>" +
        "<p style='margin:0;font-size:15px;color:" + COLOR_TEXT + ";'>Hi <strong>" + escHtml(recipientName) + "</strong>,</p>" +
        "<p style='margin:10px 0 0;font-size:14px;color:" + COLOR_TEXT_MUTED + ";line-height:1.65;'>" +
        "A new vendor invoice has been submitted and is awaiting your approval. Please review the details below and take action." +
        "</p>" +
        "</td></tr>" +

        // ── Invoice detail card ──
        "<tr><td style='padding:24px 40px;'>" +
        "<div style='background:#faf5ff;border:1px solid #ddd6fe;border-radius:12px;overflow:hidden;'>" +
        "<div style='padding:14px 20px;border-bottom:1px solid #ddd6fe;'>" +
        "<span style='font-size:11px;font-weight:700;letter-spacing:0.07em;text-transform:uppercase;color:" + COLOR_PRIMARY + ";'>Invoice Details</span>" +
        "</div>" +
        detailRow("Invoice Number", invoice.getInvoiceNumber(), true) +
        detailRow("Vendor", invoice.getVendorName(), false) +
        detailRow("Invoice Date", formatDate(invoice.getInvoiceDate()), true) +
        detailRow("Due Date", formatDate(invoice.getInvoiceDueDate()), false) +
        detailRow("PO Reference", invoice.getReferenceOur(), true) +
        detailRow("Project Code", invoice.getProjectCode(), false) +
        detailRow("Currency", invoice.getCurrency(), true) +
        detailRow("Total Amount", formatAmount(invoice.getTotalForeign(), invoice.getCurrency()), false) +
        "</div>" +
        "</td></tr>" +

        // ── Expiry notice ──
        "<tr><td style='padding:0 40px;'>" +
        "<div style='background:#fffbeb;border:1px solid #fde68a;border-radius:8px;padding:12px 16px;display:flex;align-items:center;'>" +
        "<span style='font-size:13px;color:#92400e;line-height:1.5;'>" +
        "⏱ These action links are <strong>valid for 48 hours</strong> and can only be used <strong>once</strong>. Do not share this email." +
        "</span>" +
        "</div>" +
        "</td></tr>" +

        // ── CTA buttons ──
        "<tr><td style='padding:32px 40px;'>" +
        "<p style='margin:0 0 16px;font-size:13px;font-weight:600;color:" + COLOR_TEXT_MUTED + ";text-transform:uppercase;letter-spacing:0.05em;'>Take Action</p>" +
        "<table cellpadding='0' cellspacing='0'><tr>" +
        "<td style='padding-right:12px;'>" +
        "<a href='" + approveUrl + "' style='display:inline-block;background:" + COLOR_PRIMARY + ";color:#ffffff;text-decoration:none;" +
        "padding:14px 32px;border-radius:10px;font-size:14px;font-weight:700;letter-spacing:0.01em;box-shadow:0 2px 8px rgba(124,58,237,0.35);'>" +
        "✓ &nbsp;Approve Invoice</a>" +
        "</td>" +
        "<td>" +
        "<a href='" + rejectUrl + "' style='display:inline-block;background:" + COLOR_CARD + ";color:" + COLOR_REJECT + ";text-decoration:none;" +
        "padding:13px 32px;border-radius:10px;font-size:14px;font-weight:700;border:2px solid " + COLOR_REJECT + ";letter-spacing:0.01em;'>" +
        "✕ &nbsp;Reject Invoice</a>" +
        "</td>" +
        "</tr></table>" +
        "</td></tr>" +

        // ── Divider ──
        "<tr><td style='padding:0 40px;'><hr style='border:none;border-top:1px solid " + COLOR_BORDER + ";margin:0;'></td></tr>" +

        // ── Footer ──
        "<tr><td style='padding:24px 40px 32px;'>" +
        "<p style='margin:0;font-size:12px;color:" + COLOR_TEXT_LIGHT + ";line-height:1.6;'>" +
        "This is an automated notification from AI Solutions vendor management system.<br>" +
        "Do not reply to this email. If you have questions, contact your system administrator." +
        "</p>" +
        "</td></tr>" +

        "</table>" +
        "</td></tr></table>" +
        "</body></html>";
  }

  // #region Helpers

  private static String detailRow(String label, String value, boolean shaded) {
    String bg = shaded ? "background:#f5f3ff;" : "";
    String safeValue = (value != null && !value.isBlank()) ? escHtml(value) : "<span style='color:#94a3b8;'>—</span>";
    return "<div style='display:flex;padding:11px 20px;" + bg + "border-bottom:1px solid #ede9fe;'>" +
        "<span style='font-size:13px;color:#64748b;font-weight:500;width:40%;flex-shrink:0;'>" + escHtml(label) + "</span>" +
        "<span style='font-size:13px;color:#0f172a;font-weight:600;'>" + safeValue + "</span>" +
        "</div>";
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
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
  }

  // #endregion
}
