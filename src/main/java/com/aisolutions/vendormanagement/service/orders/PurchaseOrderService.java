package com.aisolutions.vendormanagement.service.orders;

import com.aisolutions.vendormanagement.dto.PurchaseOrderDTO;
import com.aisolutions.vendormanagement.dto.PurchaseOrderDetailDTO;
import com.aisolutions.vendormanagement.repository.PurchaseOrderRepository;
import com.aisolutions.vendormanagement.service.CurrentUserService;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PurchaseOrderService {

  private static final Logger log = LoggerFactory.getLogger(PurchaseOrderService.class);

  @Inject
  PurchaseOrderRepository poRepository;

  @Inject
  CurrentUserService currentUserService;

  // #region GET METHODS

  public Uni<List<PurchaseOrderDTO>> getPurchaseOrders() {
    return currentUserService.getCurrentUser()
        .onItem().transformToUni(user -> {
          if (user == null || user.getStaffId() == null || user.getStaffId().isBlank()) {
            return Uni.createFrom().failure(
                new IllegalStateException("Unable to determine logged-in vendor"));
          }
          log.info("Fetching POs for supplier: {}", user.getStaffId());
          return poRepository.fetchPurchaseOrdersBySupplierId(user.getStaffId());
        });
  }

  public Uni<PurchaseOrderDTO> getPurchaseOrderById(Long id) {
    return currentUserService.getCurrentUser()
        .onItem().transformToUni(user -> {
          if (user == null || user.getStaffId() == null || user.getStaffId().isBlank()) {
            return Uni.createFrom().failure(
                new IllegalStateException("Unable to determine logged-in vendor"));
          }
          return poRepository.fetchPurchaseOrderById(id, user.getStaffId())
              .onItem().ifNull().failWith(() -> new IllegalArgumentException("Purchase Order not found"));
        });
  }

  public Uni<List<PurchaseOrderDetailDTO>> getPurchaseOrderDetails(Long poId) {
    return getPurchaseOrderById(poId)
        .onItem().transformToUni(po -> poRepository.fetchPurchaseOrderDetails(po.getPoNumber()));
  }

  /**
   * Get line items for a purchase order by PO number.
   * Used after verifying ownership via ID.
   */
  public Uni<List<PurchaseOrderDetailDTO>> getPurchaseOrderDetailsByPoNumber(String poNumber) {
    return poRepository.fetchPurchaseOrderDetails(poNumber);
  }

  // #endregion

  // #region Dashboard Stats

  /**
   * Get PO statistics for vendor dashboard
   */
  public Uni<Map<String, Object>> getPOStats() {
    return currentUserService.getCurrentUser()
        .onItem().transformToUni(user -> {
          if (user == null || user.getStaffId() == null || user.getStaffId().isBlank()) {
            return Uni.createFrom().failure(
                new IllegalStateException("Unable to determine logged-in vendor"));
          }
          String vendorId = user.getStaffId();
          Uni<Long> openPOCount = poRepository.countOpenPOs(vendorId);
          Uni<BigDecimal> totalValue = poRepository.getTotalOpenPOValue(vendorId);

          return Uni.combine().all().unis(openPOCount, totalValue)
              .asTuple()
              .map(tuple -> Map.of(
                  "openPurchaseOrders", tuple.getItem1(),
                  "totalOpenPOValue", tuple.getItem2()));
        });
  }

  // #endregion
}