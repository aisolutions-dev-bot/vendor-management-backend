package com.aisolutions.vendormanagement.client;

import com.aisolutions.vendormanagement.dto.VendorInvoiceApprovalConfigDTO;
import com.aisolutions.vendormanagement.service.auth.ServiceAuthHeaderFactory;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Internal REST client for org-api system parameter lookups.
 * Uses service account Basic Auth.
 */
@Path("/api/system-parameters")
@RegisterRestClient(configKey = "organization-api")
@RegisterClientHeaders(ServiceAuthHeaderFactory.class)
public interface SystemParameterInternalClient {

  @GET
  @Path("/vendor-invoice-approval")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<VendorInvoiceApprovalConfigDTO> getVendorInvoiceApprovalConfig();
}
