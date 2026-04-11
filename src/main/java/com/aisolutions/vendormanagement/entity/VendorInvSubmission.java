package com.aisolutions.vendormanagement.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "m02VendInvSubmission")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorInvSubmission {

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

  @Column(name = "InvoiceStatus", length = 15)
  private String invoiceStatus;

  @Column(name = "InvoiceNumber", length = 25)
  private String invoiceNumber;

  @Column(name = "InvoiceDate")
  private LocalDateTime invoiceDate;

  @Column(name = "Terms", length = 15)
  private String terms;

  @Column(name = "TermsDay")
  private Integer termsDay;

  @Column(name = "InvoiceDueDate")
  private LocalDateTime invoiceDueDate;

  @Column(name = "ContactType", length = 10)
  private String contactType;

  @Column(name = "VendorId", length = 25)
  private String vendorId;

  @Column(name = "VendorName", length = 100)
  private String vendorName;

  @Column(name = "VendorInvoice", length = 50)
  private String vendorInvoice;

  @Column(name = "VendorAttentionTo", length = 100)
  private String vendorAttentionTo;

  @Column(name = "Currency", length = 20)
  private String currency;

  @Column(name = "ExchangeRate", precision = 18, scale = 10)
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

  @Column(name = "Remarks", length = 500)
  private String remarks;

  @Column(name = "ExchangeRateDate")
  private LocalDateTime exchangeRateDate;

  @Column(name = "BalanceAmt", precision = 10, scale = 2)
  private BigDecimal balanceAmt;

  @Column(name = "ProjectCode", length = 25)
  private String projectCode;

  @Column(name = "ReferenceOur", length = 50)
  private String referenceOur;

  @Column(name = "InChargeStaff", length = 50)
  private String inChargeStaff;

  @Column(name = "PaymentVoucher", length = 25)
  private String paymentVoucher;

  @Column(name = "AssignedStaff", length = 30)
  private String assignedStaff;

}