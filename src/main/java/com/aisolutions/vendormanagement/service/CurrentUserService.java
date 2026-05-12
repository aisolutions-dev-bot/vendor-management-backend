package com.aisolutions.vendormanagement.service;

import com.aisolutions.vendormanagement.dto.UserDTO;
import com.aisolutions.vendormanagement.service.auth.JwtClaimsExtractor;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieves the currently logged-in user by extracting claims directly from
 * the JWT token in the incoming request — no external API call required.
 *
 * Previously this called OrganizationAuthClient#getCurrentUser which made
 * an unnecessary roundtrip to the org API and failed whenever the token
 * was not forwarded correctly, causing audit fields to store null or "SYSTEM".
 */
@ApplicationScoped
public class CurrentUserService {

  private static final Logger log = LoggerFactory.getLogger(CurrentUserService.class);

  @Inject
  JwtClaimsExtractor jwtClaimsExtractor;

  public Uni<String> getCurrentUserLoginId() {
    String staffId = jwtClaimsExtractor.extractStaffId();
    if (staffId.isBlank()) {
      log.warn("No staffId found in JWT — request may be missing Authorization header");
      return Uni.createFrom().item((String) null);
    }
    return Uni.createFrom().item(staffId);
  }

  public Uni<UserDTO> getCurrentUser() {
    String staffId = jwtClaimsExtractor.extractStaffId();
    if (staffId.isBlank()) {
      log.warn("No staffId found in JWT — request may be missing Authorization header");
      return Uni.createFrom().item((UserDTO) null);
    }
    return Uni.createFrom().item(new UserDTO(staffId, null, false));
  }
}