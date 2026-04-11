package com.aisolutions.vendormanagement.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "m02PO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "UniqId", nullable = false, updatable = false)
  private Long uniqId;

  @Column(name = "EntryStaff", length = 25)
  private String entryStaff;

  @Column(name = "EntryDate")
  private LocalDateTime entryDate;

  @Column(name = "LastEditStaff", length = 25)
  private String lastEditStaff;

  @Column(name = "LastEditDate")
  private LocalDateTime lastEditDate;

  @Column(name = "POStatus", length = 15)
  private String poStatus;

  @Column(name = "PONumber", length = 25)
  private String poNumber;

  @Column(name = "PODate")
  private LocalDateTime poDate;

  @Column(name = "RequireDate")
  private LocalDateTime requireDate;

  @Column(name = "DueDate")
  private LocalDateTime dueDate;

  @Column(name = "ReferenceOur", length = 25)
  private String referenceOur;

  @Column(name = "ReferenceYour", length = 25)
  private String referenceYour;

  @Column(name = "Terms", length = 15)
  private String terms;

  @Column(name = "TermsDay", precision = 5, scale = 0)
  private BigDecimal termsDay;

  @Column(name = "ContactType", length = 10)
  private String contactType;

  @Column(name = "SupplierID", length = 25)
  private String supplierId;

  @Column(name = "SupplierName", length = 100)
  private String supplierName;

  @Column(name = "SupplierAddress1", length = 50)
  private String supplierAddress1;

  @Column(name = "SupplierAddress2", length = 50)
  private String supplierAddress2;

  @Column(name = "SupplierAddress3", length = 50)
  private String supplierAddress3;

  @Column(name = "DeliveryAddress1", length = 50)
  private String deliveryAddress1;

  @Column(name = "DeliveryAddress2", length = 50)
  private String deliveryAddress2;

  @Column(name = "DeliveryAddress3", length = 50)
  private String deliveryAddress3;

  @Column(name = "Currency", length = 15)
  private String currency;

  @Column(name = "ExchangeRate", precision = 10, scale = 10)
  private BigDecimal exchangeRate;

  @Column(name = "InChargeStaffId", length = 25)
  private String inChargeStaffId;

  @Column(name = "TotalForeign", precision = 18, scale = 2)
  private BigDecimal totalForeign;

  @Column(name = "Remarks", length = 500)
  private String remarks;
}
