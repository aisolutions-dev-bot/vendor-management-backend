package com.aisolutions.vendormanagement.service.auth;

import com.aisolutions.vendormanagement.config.ServiceAccountConfig;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Injects Basic Auth header using a dedicated service account.
 * Used for service-to-service calls to org-api with no incoming request context.
 */
@ApplicationScoped
public class ServiceAuthHeaderFactory implements ClientHeadersFactory {

  @Inject
  ServiceAccountConfig serviceAccountConfig;

  @Override
  public MultivaluedMap<String, String> update(
      MultivaluedMap<String, String> incoming,
      MultivaluedMap<String, String> clientOutgoing) {

    String credentials = serviceAccountConfig.username() + ":" + serviceAccountConfig.password();
    String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    clientOutgoing.putSingle("Authorization", "Basic " + encoded);
    return clientOutgoing;
  }
}
