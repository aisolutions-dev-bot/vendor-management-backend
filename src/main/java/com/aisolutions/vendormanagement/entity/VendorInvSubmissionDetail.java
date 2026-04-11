package com.aisolutions.vendormanagement.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "m02VendInvSubmissionDet")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorInvSubmissionDetail {

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

  @Column(name = "InvoiceNumber", length = 25)
  private String invoiceNumber;

  @Column(name = "DetailCode", length = 4)
  private String detailCode;

  @Column(name = "ReferenceId", length = 25)
  private String referenceId;

  @Column(name = "LedgerCode", length = 10)
  private String ledgerCode;

  @Column(name = "Description", length = 200)
  private String description;

  @Column(name = "TaxType", length = 10)
  private String taxType;

  @Column(name = "TaxRate", precision = 8, scale = 4)
  private BigDecimal taxRate;

  @Column(name = "Currency", length = 20)
  private String currency;

  @Column(name = "ExchangeRate", precision = 10, scale = 10)
  private BigDecimal exchangeRate;

  @Column(name = "Quantity", precision = 8, scale = 4)
  private BigDecimal quantity;

  @Column(name = "SubTotalForeign", precision = 18, scale = 2)
  private BigDecimal subTotalForeign;

  @Column(name = "TaxForeign", precision = 8, scale = 2)
  private BigDecimal taxForeign;

  @Column(name = "TotalForeign", precision = 18, scale = 2)
  private BigDecimal totalForeign;

  @Column(name = "SubTotalBase", precision = 18, scale = 2)
  private BigDecimal subTotalBase;

  @Column(name = "TaxBase", precision = 8, scale = 2)
  private BigDecimal taxBase;

  @Column(name = "TotalBase", precision = 18, scale = 2)
  private BigDecimal totalBase;

}
