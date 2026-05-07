package com.aisolutions.vendormanagement.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VendorInvoiceApprovalConfigDTO {
  private boolean reviewEnabled;
  private String reviewStaffId;
  private String approvalStaffId;
}
