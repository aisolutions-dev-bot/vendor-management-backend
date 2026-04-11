package com.aisolutions.vendormanagement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorInvSubmissionDTO {
  private Long uniqId;
  private String entryStaff;
  private LocalDateTime entryDate;
  private String lastEditStaff;
  private LocalDateTime lastEditDate;
  private String invoiceStatus;
  private String invoiceNumber;
  private LocalDateTime invoiceDate;
  private String terms;
  private Integer termsDay;
  private LocalDateTime invoiceDueDate;
  private String contactType;
  private String vendorId;
  private String vendorName;
  private String vendorInvoice;
  private String vendorAttentionTo;
  private String currency;
  private BigDecimal exchangeRate;
  private BigDecimal subTotalForeign;
  private BigDecimal taxForeign;
  private BigDecimal totalForeign;
  private BigDecimal subTotalBase;
  private BigDecimal taxBase;
  private BigDecimal totalBase;
  private String remarks;
  private LocalDateTime exchangeRateDate;
  private BigDecimal balanceAmt;
  private String projectCode;
  private String referenceOur;
  private String inChargeStaff;
  private String paymentVoucher;
  private String assignedStaff;
}