package com.aisolutions.vendormanagement.client;

import com.aisolutions.vendormanagement.service.auth.ServiceAuthHeaderFactory;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for vendor-administration-backend system approve/reject endpoints.
 * Uses service account Basic Auth — SERVICE role required on the server side.
 */
@Path("/api/v1/vendor-invoice")
@RegisterRestClient(configKey = "vendor-admin-api")
@RegisterClientHeaders(ServiceAuthHeaderFactory.class)
public interface VendorAdminInvoiceClient {

  @PUT
  @Path("/{id}/system-approve")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  Uni<Response> systemApproveInvoice(@PathParam("id") Long id, SystemActionRequest request);

  @PUT
  @Path("/{id}/system-reject")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  Uni<Response> systemRejectInvoice(@PathParam("id") Long id, SystemActionRequest request);

  class SystemActionRequest {
    public String reason;
    public String staffId;
    public SystemActionRequest() {}
    public SystemActionRequest(String reason, String staffId) {
      this.reason = reason;
      this.staffId = staffId;
    }
  }
}
