package com.aisolutions.vendormanagement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorInvSubmissionDetailDTO {
  private Long uniqId;
  private String entryStaff;
  private LocalDateTime entryDate;
  private String lastEditStaff;
  private LocalDateTime lastEditDate;
  private String invoiceNumber;
  private String detailCode;
  private String referenceId;
  private String ledgerCode;
  private String description;
  private String taxType;
  private BigDecimal taxRate;
  private String currency;
  private BigDecimal exchangeRate;
  private BigDecimal quantity;
  private BigDecimal subTotalForeign;
  private BigDecimal taxForeign;
  private BigDecimal totalForeign;
  private BigDecimal subTotalBase;
  private BigDecimal taxBase;
  private BigDecimal totalBase;
}