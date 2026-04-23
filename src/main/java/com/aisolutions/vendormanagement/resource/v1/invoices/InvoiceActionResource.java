package com.aisolutions.vendormanagement.resource.v1.invoices;

import com.aisolutions.vendormanagement.client.VendorAdminInvoiceClient;
import com.aisolutions.vendormanagement.client.VendorAdminInvoiceClient.SystemActionRequest;
import com.aisolutions.vendormanagement.service.token.InvoiceActionTokenService;
import com.aisolutions.vendormanagement.service.token.InvoiceActionTokenService.TokenResult;
import com.aisolutions.vendormanagement.service.token.InvoiceActionTokenService.TokenValidationResult;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@Path("/api/v1/invoices/action")
public class InvoiceActionResource {

  // Colours matching AI Solutions Violet/Slate design system
  private static final String C_PRIMARY      = "#7c3aed";
  private static final String C_PRIMARY_SOFT = "#ede9fe";
  private static final String C_SUCCESS      = "#059669";
  private static final String C_SUCCESS_SOFT = "#d1fae5";
  private static final String C_SUCCESS_BORDER = "#6ee7b7";
  private static final String C_DANGER       = "#dc2626";
  private static final String C_DANGER_SOFT  = "#fee2e2";
  private static final String C_DANGER_BORDER = "#fca5a5";
  private static final String C_WARN         = "#d97706";
  private static final String C_WARN_SOFT    = "#fef3c7";
  private static final String C_WARN_BORDER  = "#fde68a";
  private static final String C_BG           = "#f8fafc";
  private static final String C_CARD         = "#ffffff";
  private static final String C_BORDER       = "#e2e8f0";
  private static final String C_TEXT         = "#0f172a";
  private static final String C_TEXT_MUTED   = "#64748b";
  private static final String C_TEXT_LIGHT   = "#94a3b8";

  @Inject
  InvoiceActionTokenService tokenService;

  @RestClient
  VendorAdminInvoiceClient vendorAdminClient;

  @GET
  @Produces(MediaType.TEXT_HTML)
  public Uni<Response> handleAction(@QueryParam("token") String token) {
    if (token == null || token.isBlank()) {
      return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
          .entity(htmlPage("Invalid Request", "No action token was provided.", "warning", "⚠"))
          .build());
    }

    return tokenService.validateAndConsume(token)
        .onItem().transformToUni(result -> {
          if (result.result == TokenResult.NOT_FOUND) {
            return Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND)
                .entity(htmlPage("Invalid Link", "This action link is invalid or does not exist.", "danger", "✕"))
                .build());
          }
          if (result.result == TokenResult.ALREADY_USED) {
            return Uni.createFrom().item(Response.status(Response.Status.GONE)
                .entity(htmlPage("Already Used", "This action link has already been used. Each link can only be used once.", "warning", "🔒"))
                .build());
          }
          if (result.result == TokenResult.EXPIRED) {
            return Uni.createFrom().item(Response.status(Response.Status.GONE)
                .entity(htmlPage("Link Expired", "This action link has expired (valid for 48 hours). Please contact the vendor to resubmit.", "warning", "⏱"))
                .build());
          }

          boolean isApprove = result.result == TokenResult.APPROVED;
          String reason  = isApprove ? "Approved via email" : "Rejected via email";
          String staffId = result.approvalStaffId != null ? result.approvalStaffId : "SYSTEM";
          String invoiceNumber = result.invoiceNumber != null ? result.invoiceNumber : "N/A";

          Uni<Response> adminCall = isApprove
              ? vendorAdminClient.systemApproveInvoice(result.invoiceId, new SystemActionRequest(reason, staffId))
              : vendorAdminClient.systemRejectInvoice(result.invoiceId, new SystemActionRequest(reason, staffId));

          return adminCall
              .onItem().transform(adminResponse -> {
                if (adminResponse.getStatus() < 400) {
                  log.info("Email action success: {} invoiceId={}", result.result, result.invoiceId);
                  if (isApprove) {
                    return Response.ok(htmlPage(
                        "Invoice Approved",
                        "Invoice <strong>#" + escHtml(invoiceNumber) + "</strong> has been successfully approved. The vendor will be notified.",
                        "success", "✓")).build();
                  } else {
                    return Response.ok(htmlPage(
                        "Invoice Rejected",
                        "Invoice <strong>#" + escHtml(invoiceNumber) + "</strong> has been rejected.",
                        "danger", "✕")).build();
                  }
                } else {
                  log.error("Vendor admin backend error: {} for invoiceId={}", adminResponse.getStatus(), result.invoiceId);
                  return Response.serverError()
                      .entity(htmlPage("Action Failed", "The action could not be completed. Please try again or contact your administrator.", "danger", "!"))
                      .build();
                }
              })
              .onFailure().recoverWithItem(e -> {
                log.error("Failed to call vendor-admin-backend for invoiceId={}: {}", result.invoiceId, e.getMessage());
                return Response.serverError()
                    .entity(htmlPage("Action Failed", "A system error occurred. Please contact your administrator.", "danger", "!"))
                    .build();
              });
        });
  }

  // #region HTML Page Builder

  private String htmlPage(String title, String message, String type, String icon) {
    String bgColor, borderColor, iconBg, iconColor, badgeColor, badgeBg;
    switch (type) {
      case "success" -> {
        bgColor = C_SUCCESS_SOFT; borderColor = C_SUCCESS_BORDER;
        iconBg = C_SUCCESS_SOFT; iconColor = C_SUCCESS;
        badgeColor = "#065f46"; badgeBg = "#d1fae5";
      }
      case "danger" -> {
        bgColor = C_DANGER_SOFT; borderColor = C_DANGER_BORDER;
        iconBg = C_DANGER_SOFT; iconColor = C_DANGER;
        badgeColor = "#7f1d1d"; badgeBg = C_DANGER_SOFT;
      }
      default -> { // warning
        bgColor = C_WARN_SOFT; borderColor = C_WARN_BORDER;
        iconBg = C_WARN_SOFT; iconColor = C_WARN;
        badgeColor = "#78350f"; badgeBg = C_WARN_SOFT;
      }
    }

    return "<!DOCTYPE html>" +
        "<html lang='en'><head><meta charset='UTF-8'>" +
        "<meta name='viewport' content='width=device-width,initial-scale=1.0'>" +
        "<title>" + escHtml(title) + "</title>" +
        "<style>" +
        "* { box-sizing: border-box; margin: 0; padding: 0; }" +
        "body { background: " + C_BG + "; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 24px; }" +
        ".card { background: " + C_CARD + "; border-radius: 20px; padding: 48px 40px; max-width: 460px; width: 100%; text-align: center; box-shadow: 0 8px 32px rgba(0,0,0,0.08); border: 1px solid " + C_BORDER + "; }" +
        ".brand { font-size: 11px; font-weight: 700; letter-spacing: 0.1em; text-transform: uppercase; color: " + C_PRIMARY + "; margin-bottom: 28px; }" +
        ".icon-wrap { width: 72px; height: 72px; border-radius: 50%; background: " + iconBg + "; border: 2px solid " + borderColor + "; display: flex; align-items: center; justify-content: center; margin: 0 auto 24px; font-size: 28px; color: " + iconColor + "; }" +
        "h1 { font-size: 22px; font-weight: 700; color: " + C_TEXT + "; letter-spacing: -0.02em; margin-bottom: 12px; }" +
        "p { font-size: 15px; color: " + C_TEXT_MUTED + "; line-height: 1.65; }" +
        ".footer { margin-top: 32px; font-size: 12px; color: " + C_TEXT_LIGHT + "; }" +
        ".divider { border: none; border-top: 1px solid " + C_BORDER + "; margin: 28px 0; }" +
        "</style>" +
        "</head><body>" +
        "<div class='card'>" +
        "<div class='brand'>AI Solutions</div>" +
        "<div class='icon-wrap'>" + icon + "</div>" +
        "<h1>" + escHtml(title) + "</h1>" +
        "<p>" + message + "</p>" +
        "<hr class='divider'>" +
        "<p class='footer'>You may close this window.</p>" +
        "</div>" +
        "</body></html>";
  }

  private static String escHtml(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
  }

  // #endregion
}
