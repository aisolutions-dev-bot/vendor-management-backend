package com.aisolutions.vendormanagement.repository;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.reactive.mutiny.Mutiny;

import com.aisolutions.vendormanagement.dto.DropdownOptionDTO;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DropdownRepository {
  @Inject
  Mutiny.SessionFactory sessionFactory;

  // ========================================
  // VENDOR QUERIES (from m02Payable)
  // ========================================

  /**
   * Get distinct vendors from m03ContactMaster table
   * Returns VendorId and VendorName for dropdown
   */
  public Uni<List<DropdownOptionDTO>> findAllVendors() {
    return sessionFactory.withSession(session -> session.createQuery(
      "SELECT DISTINCT new com.aisolutions.vendormanagement.dto.DropdownOptionDTO(" +
          "cm.contactId, cm.contactName) " +
          "FROM ContactMaster cm " +
          "ORDER BY cm.contactId",
      DropdownOptionDTO.class)
      .getResultList())
      .onFailure().invoke(e -> {
        System.err.println("Error fetching vendors: " + e.getMessage());
        e.printStackTrace();
      })
      .onFailure().recoverWithItem(e -> {
        return new ArrayList<>();
      });
  }
}
