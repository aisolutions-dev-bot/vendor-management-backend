package com.aisolutions.vendormanagement.resource.v1.invoices;

import com.aisolutions.vendormanagement.dto.CreateInvoiceRequestDTO;
import com.aisolutions.vendormanagement.dto.VendorInvSubmissionDTO;
import com.aisolutions.vendormanagement.dto.VendorInvSubmissionDetailDTO;
import com.aisolutions.vendormanagement.service.invoices.VendorInvSubmissionService;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@Path("/api/v1/vendor/invoices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VendorInvSubmissionResource {

  @Inject
  VendorInvSubmissionService invoiceService;

  // #region GET Endpoints

  /**
   * GET /api/vendor/invoices
   * Returns all invoices for the logged-in vendor
   */
  @GET
  public Uni<List<VendorInvSubmissionDTO>> getInvoices(
      @QueryParam("status") String status) {
    if (status != null && !status.isBlank()) {
      return invoiceService.getInvoicesByStatus(status);
    }
    return invoiceService.getInvoices();
  }

  /**
   * GET /api/vendor/invoices/{id}
   * Returns a single invoice by ID
   */
  @GET
  @Path("/{id}")
  public Uni<Response> getInvoiceById(@PathParam("id") Long id) {
    return invoiceService.getInvoiceById(id)
        .onItem().transform(invoice -> Response.ok(invoice).build())
        .onFailure(IllegalArgumentException.class)
        .recoverWithItem(e -> Response.status(Response.Status.NOT_FOUND)
            .entity(Map.of("error", e.getMessage())).build())
        .onFailure(IllegalStateException.class)
        .recoverWithItem(e -> Response.status(Response.Status.UNAUTHORIZED)
            .entity(Map.of("error", e.getMessage())).build());
  }

  /**
   * GET /api/vendor/invoices/{id}/details
   * Returns line items for an invoice
   */
  @GET
  @Path("/{id}/details")
  public Uni<Response> getInvoiceDetails(@PathParam("id") Long id) {
    return invoiceService.getInvoiceDetails(id)
        .onItem().transform(details -> Response.ok(details).build())
        .onFailure(IllegalArgumentException.class)
        .recoverWithItem(e -> Response.status(Response.Status.NOT_FOUND)
            .entity(Map.of("error", e.getMessage())).build())
        .onFailure(IllegalStateException.class)
        .recoverWithItem(e -> Response.status(Response.Status.UNAUTHORIZED)
            .entity(Map.of("error", e.getMessage())).build());
  }

  /**
   * GET /api/vendor/invoices/dashboard
   * Returns dashboard statistics
   */
  @GET
  @Path("/dashboard")
  public Uni<Response> getDashboardStats() {
    return invoiceService.getDashboardStats()
        .onItem().transform(stats -> Response.ok(stats).build())
        .onFailure(IllegalStateException.class)
        .recoverWithItem(e -> Response.status(Response.Status.UNAUTHORIZED)
            .entity(Map.of("error", e.getMessage())).build());
  }

  // #endregion

  // #region POST Endpoints

  /**
   * POST /api/vendor/invoices
   * Create a new invoice submission from Purchase Order
   */
  @POST
  public Uni<Response> createInvoiceFromPO(CreateInvoiceRequestDTO request) {
    return invoiceService.createInvoiceFromPO(request)
        .onItem().transform(invoice -> Response.status(Response.Status.CREATED).entity(invoice).build())
        .onFailure(IllegalArgumentException.class)
        .recoverWithItem(e -> Response.status(Response.Status.BAD_REQUEST)
            .entity(Map.of("error", e.getMessage())).build())
        .onFailure(IllegalStateException.class)
        .recoverWithItem(e -> Response.status(Response.Status.UNAUTHORIZED)
            .entity(Map.of("error", e.getMessage())).build());
  }

  /**
   * POST /api/vendor/invoices/manual
   * Create a new invoice submission with full DTO (legacy/manual entry)
   */
  @POST
  @Path("/manual")
  public Uni<Response> createInvoiceManual(CreateInvoiceManualRequest request) {
    return invoiceService.createInvoice(request.header, request.details)
        .onItem().transform(invoice -> Response.status(Response.Status.CREATED).entity(invoice).build())
        .onFailure(IllegalArgumentException.class)
        .recoverWithItem(e -> Response.status(Response.Status.BAD_REQUEST)
            .entity(Map.of("error", e.getMessage())).build())
        .onFailure(IllegalStateException.class)
        .recoverWithItem(e -> Response.status(Response.Status.UNAUTHORIZED)
            .entity(Map.of("error", e.getMessage())).build());
  }

  // #endregion

  // #region Request DTOs

  public static class CreateInvoiceManualRequest {
    public VendorInvSubmissionDTO header;
    public List<VendorInvSubmissionDetailDTO> details;
  }

  // #endregion
}