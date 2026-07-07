package com.aisolutions.vendormanagement.service;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aisolutions.vendormanagement.client.OrganizationAuthClient;
import com.aisolutions.vendormanagement.dto.UserDTO;

/**
 * Resolves the currently logged-in user by asking the org API (`/api/auth/me`),
 * which is the ONLY source that can identify a vendor: a vendor's JWT carries an
 * EMPTY `staffId` claim (org-api `resolveStaffId` returns "" for loginType=vendor),
 * so the vendor id lives solely in `VendorUserLogin` and is returned by `/me`.
 *
 * A prior refactor switched this to read `staffId` from the JWT directly to avoid
 * the roundtrip; that works for staff but returns null for every vendor login,
 * breaking the whole vendor portal ("Unable to determine logged-in vendor").
 */
@ApplicationScoped
public class CurrentUserService {

  private static final Logger log = LoggerFactory.getLogger(CurrentUserService.class);

  @Inject
  @RestClient
  OrganizationAuthClient authClient;

  public Uni<String> getCurrentUserLoginId() {
    return authClient.getCurrentUser()
        .onFailure().invoke(e -> {
          log.error("Failed to retrieve current user from auth service: {}", e.getMessage());
          e.printStackTrace();
        })
        .onFailure().recoverWithItem((UserDTO) null)
        .onItem().transform(user -> {
          if (user == null) {
            log.warn("Current user is null - auth service may be unavailable");
            return null;
          }
          return user.getSecLoginId();
        });
  }

  public Uni<UserDTO> getCurrentUser() {
    return authClient.getCurrentUser()
        .onFailure().invoke(e -> {
          log.error("Failed to retrieve current user from auth service: {}", e.getMessage());
          e.printStackTrace();
        })
        .onFailure().recoverWithItem((UserDTO) null);
  }
}
