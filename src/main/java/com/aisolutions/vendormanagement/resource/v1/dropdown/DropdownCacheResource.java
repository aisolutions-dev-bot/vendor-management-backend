package com.aisolutions.vendormanagement.resource.v1.dropdown;

import java.util.Map;

import com.aisolutions.vendormanagement.service.dropdown.DropdownCacheService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/api/v1/cache")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DropdownCacheResource {
  
  @Inject
  DropdownCacheService dropdownCacheService;

  /**
     * Clear all dropdown cache
     */
    @DELETE
    @Path("/dropdown")
    public Response clearAllCache() {
        log.info("Clearing all dropdown cache");
        dropdownCacheService.clearCache();
        return Response.ok(Map.of(
            "success", true,
            "message", "All dropdown cache cleared successfully"
        )).build();
    }

    /**
     * Clear vendor dropdown cache
     */
    @DELETE
    @Path("/dropdown/vendor")
    public Response clearVendorCache() {
        log.info("Clearing vendor dropdown cache");
        dropdownCacheService.clearVendorCache();
        return Response.ok(Map.of(
            "success", true,
            "message", "Vendors dropdown cache cleared successfully"
        )).build();
    }
}
