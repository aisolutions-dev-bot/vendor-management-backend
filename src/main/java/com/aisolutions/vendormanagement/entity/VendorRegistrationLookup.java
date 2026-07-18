package com.aisolutions.vendormanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal read-only mapping onto the shared m03VendorRegistration table,
 * used only to resolve a vendor's own email/name for invoice notifications
 * (the vendorId on an invoice is this table's VendorId).
 */
@Entity
@Table(name = "m03VendorRegistration")
@Data
@NoArgsConstructor
public class VendorRegistrationLookup {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "UniqId", nullable = false, updatable = false)
  private Long uniqId;

  @Column(name = "VendorId", length = 25)
  private String vendorId;

  @Column(name = "VendorName", length = 50)
  private String vendorName;

  @Column(name = "Email", length = 100)
  private String email;
}
