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

  @Inject
  InvoiceActionTokenService tokenService;

  @RestClient
  VendorAdminInvoiceClient vendorAdminClient;

  /**
   * GET /api/v1/invoices/action?token=xxx
   * Called when admin clicks Approve or Reject button in email.
   * Validates token, calls vendor-administration-backend, returns HTML confirmation.
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  public Uni<Response> handleAction(@QueryParam("token") String token) {
    if (token == null || token.isBlank()) {
      return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
          .entity(htmlPage("Invalid Request", "No action token provided.", false))
          .build());
    }

    return tokenService.validateAndConsume(token)
        .onItem().transformToUni(result -> {
          if (result.result == TokenResult.NOT_FOUND) {
            return Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND)
                .entity(htmlPage("Invalid Link", "This action link is invalid or does not exist.", false))
                .build());
          }
          if (result.result == TokenResult.ALREADY_USED) {
            return Uni.createFrom().item(Response.status(Response.Status.GONE)
                .entity(htmlPage("Already Used", "This action link has already been used.", false))
                .build());
          }
          if (result.result == TokenResult.EXPIRED) {
            return Uni.createFrom().item(Response.status(Response.Status.GONE)
                .entity(htmlPage("Link Expired", "This action link has expired. Please ask the vendor to resubmit.", false))
                .build());
          }

          // Token valid — call vendor-admin-backend
          boolean isApprove = result.result == TokenResult.APPROVED;
          String reason = isApprove ? "Approved via email" : "Rejected via email";
          String invoiceNumber = result.invoiceNumber != null ? result.invoiceNumber : "N/A";

          String staffId = result.approvalStaffId != null ? result.approvalStaffId : "SYSTEM";
          Uni<Response> adminCall = isApprove
              ? vendorAdminClient.systemApproveInvoice(result.invoiceId, new SystemActionRequest(reason, staffId))
              : vendorAdminClient.systemRejectInvoice(result.invoiceId, new SystemActionRequest(reason, staffId));

          return adminCall
              .onItem().transform(adminResponse -> {
                if (adminResponse.getStatus() < 400) {
                  String title = isApprove ? "Invoice Approved" : "Invoice Rejected";
                  String message = isApprove
                      ? "Invoice <strong>#" + escHtml(invoiceNumber) + "</strong> has been successfully approved."
                      : "Invoice <strong>#" + escHtml(invoiceNumber) + "</strong> has been rejected.";
                  log.info("Email action success: {} invoiceId={}", result.result, result.invoiceId);
                  return Response.ok(htmlPage(title, message, isApprove)).build();
                } else {
                  log.error("Vendor admin backend returned error: {} for invoiceId={}", adminResponse.getStatus(), result.invoiceId);
                  return Response.serverError()
                      .entity(htmlPage("Action Failed", "The action could not be completed. Please try again or contact your administrator.", false))
                      .build();
                }
              })
              .onFailure().recoverWithItem(e -> {
                log.error("Failed to call vendor-admin-backend for invoiceId={}: {}", result.invoiceId, e.getMessage());
                return Response.serverError()
                    .entity(htmlPage("Action Failed", "The action could not be completed. Please contact your administrator.", false))
                    .build();
              });
        });
  }

  // #region HTML Helpers

  private static String htmlPage(String title, String message, boolean success) {
    String iconColor = success ? "#10b981" : "#ef4444";
    String icon = success ? "✓" : "✕";
    String bgColor = success ? "#f0fdf4" : "#fef2f2";
    String borderColor = success ? "#bbf7d0" : "#fecaca";

    return "<!DOCTYPE html>" +
        "<html lang='en'><head><meta charset='UTF-8'>" +
        "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
        "<title>" + escHtml(title) + "</title></head>" +
        "<body style='margin:0;padding:0;background:#f4f6f9;font-family:-apple-system,BlinkMacSystemFont,\"Segoe UI\",Roboto,sans-serif;display:flex;align-items:center;justify-content:center;min-height:100vh;'>" +
        "<div style='background:#ffffff;border-radius:16px;padding:48px 40px;max-width:440px;width:90%;text-align:center;box-shadow:0 4px 24px rgba(0,0,0,0.08);'>" +
        "<div style='width:64px;height:64px;border-radius:50%;background:" + bgColor + ";border:2px solid " + borderColor + ";display:flex;align-items:center;justify-content:center;margin:0 auto 24px;font-size:28px;color:" + iconColor + ";'>" +
        icon + "</div>" +
        "<h1 style='margin:0 0 12px;font-size:22px;font-weight:700;color:#1e293b;'>" + escHtml(title) + "</h1>" +
        "<p style='margin:0;font-size:15px;color:#475569;line-height:1.6;'>" + message + "</p>" +
        "<p style='margin:24px 0 0;font-size:12px;color:#94a3b8;'>You may close this window.</p>" +
        "</div></body></html>";
  }

  private static String escHtml(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
  }

  // #endregion
}
