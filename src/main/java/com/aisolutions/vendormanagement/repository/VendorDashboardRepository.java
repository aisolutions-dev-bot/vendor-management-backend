package com.aisolutions.vendormanagement.repository;

import com.aisolutions.vendormanagement.dto.VendorDashboardDTO;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@ApplicationScoped
@WithSession
public class VendorDashboardRepository implements PanacheRepositoryBase<Object, Long> {

  /**
   * Get all dashboard stats in a single query using conditional COUNTs.
   * This uses only ONE database connection instead of 5 parallel queries.
   */
  public Uni<VendorDashboardDTO> getDashboardStats(String vendorId, LocalDate fromDate, LocalDate toDate) {
    LocalDateTime fromDateTime = fromDate.atStartOfDay();
    LocalDateTime toDateTime = toDate.atTime(23, 59, 59);

    // Single query with conditional counts for PO statuses
    String poCountsSql = """
        SELECT
            SUM(CASE WHEN p.poStatus = 'OPEN' THEN 1 ELSE 0 END) as openPO,
            SUM(CASE WHEN p.poStatus = 'SUBMIT' THEN 1 ELSE 0 END) as pendingVerification,
            SUM(CASE WHEN p.poStatus = 'REVIEW' THEN 1 ELSE 0 END) as pendingApproval,
            SUM(CASE WHEN p.poStatus = 'APPROVED' THEN 1 ELSE 0 END) as pendingPayment
        FROM PurchaseOrder p
        WHERE p.supplierId = :vendorId
          AND p.poDate >= :fromDate
          AND p.poDate <= :toDate
        """;

    // Paid invoices from m02Payable table where PaidStatus = 'Paid'
    String paidInvoicesSql = """
        SELECT COUNT(p)
        FROM Payable p
        WHERE p.vendorId = :vendorId
          AND p.invoiceStatus = 'PAID'
          AND p.invoiceDate >= :fromDate
          AND p.invoiceDate <= :toDate
        """;

    return getSession().flatMap(session -> {
      // First query: PO counts
      Uni<Object[]> poCountsUni = session.createSelectionQuery(poCountsSql, Object[].class)
          .setParameter("vendorId", vendorId)
          .setParameter("fromDate", fromDateTime)
          .setParameter("toDate", toDateTime)
          .getSingleResultOrNull()
          .map(result -> result != null ? result : new Object[] { 0L, 0L, 0L, 0L });

      // Second query: Paid invoices count (chained, same session)
      return poCountsUni.flatMap(poCounts -> session.createSelectionQuery(paidInvoicesSql, Long.class)
          .setParameter("vendorId", vendorId)
          .setParameter("fromDate", fromDateTime)
          .setParameter("toDate", toDateTime)
          .getSingleResultOrNull()
          .map(paidCount -> {
            // Build DTO from results
            return VendorDashboardDTO.builder()
                .openPO(toLong(poCounts[0]))
                .pendingVerification(toLong(poCounts[1]))
                .pendingApproval(toLong(poCounts[2]))
                .pendingPayment(toLong(poCounts[3]))
                .paidInvoices(paidCount != null ? paidCount : 0L)
                .build();
          }));
    });
  }

  private Long toLong(Object value) {
    if (value == null)
      return 0L;
    if (value instanceof Long)
      return (Long) value;
    if (value instanceof Number)
      return ((Number) value).longValue();
    return 0L;
  }
}