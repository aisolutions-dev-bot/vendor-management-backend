package com.aisolutions.vendormanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorDashboardDTO {

  // Count of POs with status = 'Open'
  private Long openPO;

  // Count of POs with status = 'Submit' (pending verification)
  private Long pendingVerification;

  // Count of POs with status = 'Review' (pending approval)
  private Long pendingApproval;

  // Count of POs with status = 'Approved' (pending payment)
  private Long pendingPayment;

  // Count of invoices with paidStatus = 'Paid'
  private Long paidInvoices;
}