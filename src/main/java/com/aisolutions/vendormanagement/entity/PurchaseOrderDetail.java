package com.aisolutions.vendormanagement.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "m02PoDet")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDetail {

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

  @Column(name = "PoNumber", length = 25)
  private String poNumber;

  @Column(name = "OpCode", length = 4)
  private String opCode;

  @Column(name = "MainRefCode", length = 25)
  private String mainRefCode;

  @Column(name = "RefType", length = 25)
  private String refType;

  @Column(name = "BillStatus", length = 1)
  private String billStatus;

  @Column(name = "DeliverStatus", length = 1)
  private String deliverStatus;

  @Column(name = "CostCode", length = 50)
  private String costCode;

  @Column(name = "ItemCode", length = 50)
  private String itemCode;

  @Column(name = "DetailCode", length = 50)
  private String detailCode;

  @Column(name = "Sequence", length = 7)
  private String sequence;

  @Column(name = "Description", length = 200)
  private String description;

  @Column(name = "Comments", length = 4000)
  private String comments;

  @Column(name = "Currency", length = 20)
  private String currency;

  @Column(name = "QuantityOrder", precision = 18, scale = 4)
  private BigDecimal quantityOrder;

  @Column(name = "QuantityIn", precision = 18, scale = 4)
  private BigDecimal quantityIn;

  @Column(name = "QuantityBill", precision = 18, scale = 4)
  private BigDecimal quantityBill;

  @Column(name = "UnitMs", length = 10)
  private String unitMs;

  @Column(name = "UnitMsForeign", precision = 18, scale = 4)
  private BigDecimal unitMsForeign;

  @Column(name = "SubTotalForeign", precision = 18, scale = 2)
  private BigDecimal subTotalForeign;

  @Column(name = "TotalBill", precision = 18, scale = 2)
  private BigDecimal totalBill;

  @Column(name = "LedgerCode", length = 10)
  private String ledgerCode;

  @Column(name = "LedgerName", length = 100)
  private String ledgerName;

  @Column(name = "SubType", length = 10)
  private String subType;

  @Column(name = "DiscPct", precision = 10, scale = 4)
  private BigDecimal discPct;

  @Column(name = "SubContractID", length = 25)
  private String subContractId;

  @Column(name = "PartNo", length = 30)
  private String partNo;

  @Column(name = "Source", length = 10)
  private String source;

  @Column(name = "VendPartNo", length = 30)
  private String vendPartNo;

  @Column(name = "StaffId", length = 25)
  private String staffId;

  @Column(name = "GroupID", length = 10)
  private String groupId;

  @Column(name = "DetailID", length = 10)
  private String detailId;

  @Column(name = "GrpDetName", length = 200)
  private String grpDetName;

  @Column(name = "SubLedgerCode", length = 6)
  private String subLedgerCode;

  @Column(name = "PrNo", length = 25)
  private String prNo;

  @Column(name = "PrUniqId", precision = 18, scale = 0)
  private BigDecimal prUniqId;

  @Column(name = "PrRequiredDate")
  private LocalDateTime prRequiredDate;

  @Column(name = "SupplierPartNo", length = 30)
  private String supplierPartNo;

  @Column(name = "CutOffQtyIn", precision = 18, scale = 4)
  private BigDecimal cutOffQtyIn;

  @Column(name = "CufOffGRNOValue", precision = 18, scale = 2)
  private BigDecimal cufOffGRNOValue;

  @Column(name = "CutOffGRNValue", precision = 18, scale = 2)
  private BigDecimal cutOffGRNValue;

  @Column(name = "GRNQuantityIn", precision = 18, scale = 4)
  private BigDecimal grnQuantityIn;

  @Column(name = "RequestedBy", length = 25)
  private String requestedBy;

  @Column(name = "RefSQLCode", precision = 18, scale = 0)
  private BigDecimal refSQLCode;

  @Column(name = "RefDocNo", length = 25)
  private String refDocNo;

  @Column(name = "NoOfCarton", precision = 18, scale = 4)
  private BigDecimal noOfCarton;

}