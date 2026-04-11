package com.aisolutions.vendormanagement.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simplified request for creating invoice from Purchase Order.
 * The backend will fetch PO details and create invoice line items automatically.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceRequestDTO {
    
    private Long poUniqId;
    private String poNumber;
    private String currency;
    private BigDecimal exchangeRate;
    private BigDecimal totalForeign;
}