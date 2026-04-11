package com.aisolutions.vendormanagement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDetailDTO {
  private Long uniqId;
  private String entryStaff;
  private LocalDateTime entryDate;
  private String lastEditStaff;
  private LocalDateTime lastEditDate;
  private String poNumber;
  private String opCode;
  private String mainRefCode;
  private String refType;
  private String billStatus;
  private String deliverStatus;
  private String costCode;
  private String itemCode;
  private String detailCode;
  private String sequence;
  private String description;
  private String comments;
  private String currency;
  private BigDecimal quantityOrder;
  private BigDecimal quantityIn;
  private BigDecimal quantityBill;
  private String unitMs;
  private BigDecimal unitMsForeign;
  private BigDecimal subTotalForeign;
  private BigDecimal totalBill;
  private String ledgerCode;
  private String ledgerName;
  private String subType;
  private BigDecimal discPct;
  private String subContractId;
  private String partNo;
  private String source;
  private String vendPartNo;
  private String staffId;
  private String groupId;
  private String detailId;
  private String grpDetName;
  private String subLedgerCode;
  private String prNo;
  private BigDecimal prUniqId;
  private LocalDateTime prRequiredDate;
  private String supplierPartNo;
  private BigDecimal cutOffQtyIn;
  private BigDecimal cufOffGRNOValue;
  private BigDecimal cutOffGRNValue;
  private BigDecimal grnQuantityIn;
  private String requestedBy;
  private BigDecimal refSQLCode;
  private String refDocNo;
  private BigDecimal noOfCarton;

  // Custom constructor for summary queries
  public PurchaseOrderDetailDTO(Long uniqId, String poNumber, String sequence, 
                                String description, BigDecimal quantityOrder,
                                String unitMs, BigDecimal unitMsForeign, 
                                BigDecimal subTotalForeign) {
    this.uniqId = uniqId;
    this.poNumber = poNumber;
    this.sequence = sequence;
    this.description = description;
    this.quantityOrder = quantityOrder;
    this.unitMs = unitMs;
    this.unitMsForeign = unitMsForeign;
    this.subTotalForeign = subTotalForeign;
  }
}