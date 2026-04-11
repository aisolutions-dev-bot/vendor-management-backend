package com.aisolutions.vendormanagement.service.dashboard;

import com.aisolutions.vendormanagement.dto.VendorDashboardDTO;
import com.aisolutions.vendormanagement.repository.VendorDashboardRepository;
import com.aisolutions.vendormanagement.service.CurrentUserService;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Slf4j
@ApplicationScoped
public class VendorDashboardService {

  @Inject
  VendorDashboardRepository dashboardRepository;

  @Inject
  CurrentUserService currentUserService;

  // TODO: Remove hardcode after vendor auth is implemented
  private static final String HARDCODED_VENDOR_ID = "EVECONPL01";

  /**
   * Get dashboard statistics for the logged-in vendor.
   * Uses a single database query to minimize connection usage.
   */
  public Uni<VendorDashboardDTO> getStats(LocalDate fromDate, LocalDate toDate) {
    // TODO: Remove hardcode after vendor auth is implemented
    String vendorId = HARDCODED_VENDOR_ID;

    log.info("Fetching dashboard stats for vendor: {} from {} to {}", vendorId, fromDate, toDate);

    return dashboardRepository.getDashboardStats(vendorId, fromDate, toDate)
        .onFailure().recoverWithItem(() -> {
          log.error("Failed to fetch dashboard stats, returning zeros");
          return VendorDashboardDTO.builder()
              .openPO(0L)
              .pendingVerification(0L)
              .pendingApproval(0L)
              .pendingPayment(0L)
              .paidInvoices(0L)
              .build();
        });

    // TODO: Uncomment after vendor auth is implemented
    // return currentUserService.getCurrentUserLoginId()
    // .onItem().transformToUni(vendorId -> {
    // if (vendorId == null || vendorId.isBlank()) {
    // return Uni.createFrom().failure(
    // new IllegalStateException("Unable to determine logged-in vendor"));
    // }
    // return dashboardRepository.getDashboardStats(vendorId, fromDate, toDate);
    // });
  }
}