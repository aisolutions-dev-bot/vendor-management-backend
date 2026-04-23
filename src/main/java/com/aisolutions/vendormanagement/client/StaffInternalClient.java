package com.aisolutions.vendormanagement.client;

import com.aisolutions.vendormanagement.dto.StaffDTO;
import com.aisolutions.vendormanagement.service.auth.ServiceAuthHeaderFactory;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Internal REST client for org-api staff lookup.
 * Uses service account Basic Auth (no incoming request context needed).
 */
@Path("/api/staff")
@RegisterRestClient(configKey = "organization-api")
@RegisterClientHeaders(ServiceAuthHeaderFactory.class)
public interface StaffInternalClient {

  @GET
  @Path("/{staffId}")
  @Produces(MediaType.APPLICATION_JSON)
  Uni<StaffDTO> getStaff(@PathParam("staffId") String staffId);
}
