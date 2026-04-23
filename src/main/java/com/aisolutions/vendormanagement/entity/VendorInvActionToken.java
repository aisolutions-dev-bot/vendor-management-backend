package com.aisolutions.vendormanagement.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "m02VendInvActionToken")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorInvActionToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "UniqId", nullable = false, updatable = false)
  private Long uniqId;

  @Column(name = "Token", length = 64, nullable = false, unique = true)
  private String token;

  @Column(name = "InvoiceId", nullable = false)
  private Long invoiceId;

  @Column(name = "InvoiceNumber", length = 25)
  private String invoiceNumber;

  @Column(name = "Action", length = 10, nullable = false)
  private String action; // APPROVE or REJECT

  @Column(name = "ApprovalStaffId", length = 25)
  private String approvalStaffId;

  @Column(name = "ExpiresAt", nullable = false)
  private LocalDateTime expiresAt;

  @Column(name = "UsedAt")
  private LocalDateTime usedAt;

  @Column(name = "CreatedAt", nullable = false)
  private LocalDateTime createdAt;
}
