package com.aisolutions;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool; // generic reactive pool
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/health")
public class HealthResource {

  @Inject
  Pool client; // <- reactive pool

  @GET
  public Uni<Response> health() {
    return client.query("SELECT 1")
        .execute()
        .onItem().transform(rs -> Response.ok("UP").build());
  }
}
