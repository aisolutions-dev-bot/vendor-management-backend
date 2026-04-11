package com.aisolutions.vendormanagement.repository;

import com.aisolutions.vendormanagement.dto.PurchaseOrderDTO;
import com.aisolutions.vendormanagement.dto.PurchaseOrderDetailDTO;
import com.aisolutions.vendormanagement.entity.PurchaseOrder;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@ApplicationScoped
@WithSession
public class PurchaseOrderRepository implements PanacheRepositoryBase<PurchaseOrder, Long> {

  // #region m02PO Queries

  private static final String PO_SELECT = """
      SELECT new com.aisolutions.vendormanagement.dto.PurchaseOrderDTO(
          p.uniqId, p.entryStaff, p.entryDate, p.lastEditStaff, p.lastEditDate,
          p.poStatus, p.poNumber, p.poDate, p.requireDate, p.dueDate, p.referenceOur,
          p.referenceYour, p.terms, p.termsDay, p.contactType, p.supplierId,
          p.supplierName, p.supplierAddress1, p.supplierAddress2, p.supplierAddress3,
          p.deliveryAddress1, p.deliveryAddress2, p.deliveryAddress3, p.currency,
          p.exchangeRate, p.inChargeStaffId, p.totalForeign, p.remarks
      )
      FROM PurchaseOrder p
      """;

  /**
   * Get purchase orders for a specific supplier (vendor) with POStatus = 'O'
   * (Open)
   */
  public Uni<List<PurchaseOrderDTO>> fetchPurchaseOrdersBySupplierId(String supplierId) {
    return getSession().flatMap(session -> session.createQuery(
        PO_SELECT +
            "WHERE p.supplierId = :supplierId AND p.poStatus = 'OPEN' " +
            "ORDER BY p.poDate DESC",
        PurchaseOrderDTO.class)
        .setParameter("supplierId", supplierId)
        .getResultList());
  }

  /**
   * Get a single purchase order by ID with supplier ownership check
   */
  public Uni<PurchaseOrderDTO> fetchPurchaseOrderById(Long id, String supplierId) {
    return getSession().flatMap(session -> session.createQuery(
        PO_SELECT +
            "WHERE p.uniqId = :id AND p.supplierId = :supplierId",
        PurchaseOrderDTO.class)
        .setParameter("id", id)
        .setParameter("supplierId", supplierId)
        .getSingleResultOrNull());
  }

  // #endregion

  // #region m02PoDet Queries - Updated for new table structure

  private static final String PO_DET_SELECT = """
      SELECT new com.aisolutions.vendormanagement.dto.PurchaseOrderDetailDTO(
          d.uniqId, d.entryStaff, d.entryDate, d.lastEditStaff, d.lastEditDate,
          d.poNumber, d.opCode, d.mainRefCode, d.refType, d.billStatus,
          d.deliverStatus, d.costCode, d.itemCode, d.detailCode, d.sequence,
          d.description, d.comments, d.currency, d.quantityOrder, d.quantityIn,
          d.quantityBill, d.unitMs, d.unitMsForeign, d.subTotalForeign, d.totalBill,
          d.ledgerCode, d.ledgerName, d.subType, d.discPct, d.subContractId,
          d.partNo, d.source, d.vendPartNo, d.staffId, d.groupId, d.detailId,
          d.grpDetName, d.subLedgerCode, d.prNo, d.prUniqId, d.prRequiredDate,
          d.supplierPartNo, d.cutOffQtyIn, d.cufOffGRNOValue, d.cutOffGRNValue,
          d.grnQuantityIn, d.requestedBy, d.refSQLCode, d.refDocNo, d.noOfCarton
      )
      FROM PurchaseOrderDetail d
      """;

  /**
   * Get purchase order details by PO number
   */
  public Uni<List<PurchaseOrderDetailDTO>> fetchPurchaseOrderDetails(String poNumber) {
    return getSession().flatMap(session -> session.createQuery(
        PO_DET_SELECT +
            "WHERE d.poNumber = :poNumber " +
            "ORDER BY d.sequence",
        PurchaseOrderDetailDTO.class)
        .setParameter("poNumber", poNumber)
        .getResultList());
  }

  /**
   * Get purchase order details by PO ID (first fetches the PO number)
   */
  public Uni<List<PurchaseOrderDetailDTO>> fetchPurchaseOrderDetailsById(Long poId, String supplierId) {
    return fetchPurchaseOrderById(poId, supplierId)
        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Purchase Order not found"))
        .onItem().transformToUni(po -> fetchPurchaseOrderDetails(po.getPoNumber()));
  }

  /**
   * Get summary of PO details (simplified view for listing)
   */
  public Uni<List<PurchaseOrderDetailDTO>> fetchPurchaseOrderDetailsSummary(String poNumber) {
    return getSession().flatMap(session -> session.createQuery("""
        SELECT new com.aisolutions.vendormanagement.dto.PurchaseOrderDetailDTO(
            d.uniqId, d.poNumber, d.sequence, d.description, d.quantityOrder,
            d.unitMs, d.unitMsForeign, d.subTotalForeign
        )
        FROM PurchaseOrderDetail d
        WHERE d.poNumber = :poNumber
        ORDER BY d.sequence
        """, PurchaseOrderDetailDTO.class)
        .setParameter("poNumber", poNumber)
        .getResultList());
  }

  // #endregion

  // #region Dashboard Stats

  /**
   * Count open POs for a vendor
   */
  public Uni<Long> countOpenPOs(String supplierId) {
    return getSession().flatMap(session -> session.createQuery("""
        SELECT COUNT(p) FROM PurchaseOrder p
        WHERE p.supplierId = :supplierId AND p.poStatus = 'O'
        """, Long.class)
        .setParameter("supplierId", supplierId)
        .getSingleResult());
  }

  /**
   * Get total outstanding PO value for a vendor
   */
  public Uni<java.math.BigDecimal> getTotalOpenPOValue(String supplierId) {
    return getSession().flatMap(session -> session.createQuery("""
        SELECT COALESCE(SUM(p.totalForeign), 0) FROM PurchaseOrder p
        WHERE p.supplierId = :supplierId AND p.poStatus = 'O'
        """, java.math.BigDecimal.class)
        .setParameter("supplierId", supplierId)
        .getSingleResult());
  }

  // #endregion
}