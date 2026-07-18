package com.aisolutions.vendormanagement.repository;

import com.aisolutions.vendormanagement.entity.VendorRegistrationLookup;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@WithSession
public class VendorRegistrationLookupRepository implements PanacheRepositoryBase<VendorRegistrationLookup, Long> {

  /**
   * Fetch vendor email by vendorId (used for invoice vendor-ack notifications)
   */
  public Uni<String> fetchEmailByVendorId(String vendorId) {
    return getSession().flatMap(session ->
        session.createSelectionQuery(
            "SELECT r.email FROM VendorRegistrationLookup r WHERE r.vendorId = :vendorId",
            String.class)
        .setParameter("vendorId", vendorId)
        .getSingleResultOrNull()
    );
  }
}
