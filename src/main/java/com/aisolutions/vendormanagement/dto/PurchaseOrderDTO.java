package com.aisolutions.vendormanagement.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDTO {

    private Long uniqId;
    private String entryStaff;
    private LocalDateTime entryDate;
    private String lastEditStaff;
    private LocalDateTime lastEditDate;
    private String poStatus;
    private String poNumber;
    private LocalDateTime poDate;
    private LocalDateTime requireDate;
    private LocalDateTime dueDate;
    private String referenceOur;
    private String referenceYour;
    private String terms;
    private Integer termsDay;
    private String contactType;
    private String supplierId;
    private String supplierName;
    private String supplierAddress1;
    private String supplierAddress2;
    private String supplierAddress3;
    private String deliveryAddress1;
    private String deliveryAddress2;
    private String deliveryAddress3;
    private String currency;
    private BigDecimal exchangeRate;
    private String inChargeStaffId;
    private BigDecimal totalForeign;
    private String remarks;
}
