package com.aisolutions.vendormanagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "m02Payable")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "UniqId", nullable = false, updatable = false)
  private Long uniqId;

  @Column(name = "EntryStaff")
  private String entryStaff;

  @Column(name = "EntryDate")
  private LocalDateTime entryDate;

  @Column(name = "LastEditStaff")
  private String lastEditStaff;

  @Column(name = "LastEditDate")
  private LocalDateTime lastEditDate;

  @Column(name = "InvoiceStatus")
  private String invoiceStatus;

  @Column(name = "InvoiceNumber")
  private String invoiceNumber;

  @Column(name = "InvoiceDate")
  private LocalDateTime invoiceDate;

  @Column(name = "Terms")
  private String terms;

  @Column(name = "TermsDay")
  private Integer termsDay;

  @Column(name = "InvoiceDueDate")
  private LocalDateTime invoiceDueDate;

  @Column(name = "ContactType")
  private String contactType;

  @Column(name = "VendorId")
  private String vendorId;

  @Column(name = "VendorName")
  private String vendorName;

  @Column(name = "VendorInvoice")
  private String vendorInvoice;

  @Column(name = "VendorAttentionTo")
  private String vendorAttentionTo;

  @Column(name = "Currency")
  private String currency;

  @Column(name = "ExchangeRate", precision = 18, scale = 6)
  private BigDecimal exchangeRate;

  @Column(name = "SubTotalForeign", precision = 18, scale = 2)
  private BigDecimal subTotalForeign;

  @Column(name = "TaxForeign", precision = 18, scale = 2)
  private BigDecimal taxForeign;

  @Column(name = "TotalForeign", precision = 18, scale = 2)
  private BigDecimal totalForeign;

  @Column(name = "SubTotalBase", precision = 18, scale = 2)
  private BigDecimal subTotalBase;

  @Column(name = "TaxBase", precision = 18, scale = 2)
  private BigDecimal taxBase;

  @Column(name = "TotalBase", precision = 18, scale = 2)
  private BigDecimal totalBase;

  @Column(name = "Remarks")
  private String remarks;

  @Column(name = "ExchangeRateDate")
  private LocalDateTime exchangeRateDate;
}