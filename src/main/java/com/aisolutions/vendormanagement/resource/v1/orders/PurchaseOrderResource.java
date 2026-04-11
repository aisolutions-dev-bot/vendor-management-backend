package com.aisolutions.vendormanagement.resource.v1.orders;

import com.aisolutions.vendormanagement.dto.PurchaseOrderDTO;
import com.aisolutions.vendormanagement.service.orders.PurchaseOrderService;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/v1/vendor/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PurchaseOrderResource {

  @Inject
  PurchaseOrderService poService;

  // #region GET ENDPOINTS

  @GET
  public Uni<List<PurchaseOrderDTO>> getOrders() {
    return poService.getPurchaseOrders();
  }

  @GET
  @Path("/{id}")
  public Uni<Response> getOrderById(@PathParam("id") Long id) {
    return poService.getPurchaseOrderById(id)
        .onItem().transform(po -> Response.ok(po).build())
        .onFailure(IllegalArgumentException.class)
        .recoverWithItem(e -> Response.status(Response.Status.NOT_FOUND)
            .entity(e.getMessage()).build())
        .onFailure(IllegalStateException.class)
        .recoverWithItem(e -> Response.status(Response.Status.UNAUTHORIZED)
            .entity(e.getMessage()).build());
  }

  @GET
  @Path("/{id}/details")
  public Uni<Response> getOrderDetails(@PathParam("id") Long id) {
    return poService.getPurchaseOrderDetails(id)
        .onItem().transform(details -> Response.ok(details).build())
        .onFailure(IllegalArgumentException.class)
        .recoverWithItem(e -> Response.status(Response.Status.NOT_FOUND)
            .entity(e.getMessage()).build())
        .onFailure(IllegalStateException.class)
        .recoverWithItem(e -> Response.status(Response.Status.UNAUTHORIZED)
            .entity(e.getMessage()).build());
  }

  // #endregion
}