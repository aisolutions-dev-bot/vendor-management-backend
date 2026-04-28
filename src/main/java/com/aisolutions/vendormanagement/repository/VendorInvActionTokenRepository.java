package com.aisolutions.vendormanagement.repository;

import com.aisolutions.vendormanagement.entity.VendorInvActionToken;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@ApplicationScoped
@WithSession
public class VendorInvActionTokenRepository implements PanacheRepositoryBase<VendorInvActionToken, Long> {

  /**
   * Persist a new action token.
   */
  public Uni<VendorInvActionToken> insert(VendorInvActionToken token) {
    return persist(token);
  }

  /**
   * Find token by token string. Returns null if not found.
   */
  public Uni<VendorInvActionToken> findByToken(String token) {
    return find("token", token).firstResult();
  }

  /**
   * Mark token as used (one-time enforcement).
   */
  public Uni<Integer> markUsed(String token, LocalDateTime usedAt) {
    return update("usedAt = ?1 where token = ?2 and usedAt is null", usedAt, token);
  }
}
