package com.aisolutions.vendormanagement.repository;

import com.aisolutions.vendormanagement.dto.PurchaseOrderDTO;
import com.aisolutions.vendormanagement.dto.PurchaseOrderDetailDTO;
import com.aisolutions.vendormanagement.dto.VendorInvSubmissionDTO;
import com.aisolutions.vendormanagement.dto.VendorInvSubmissionDetailDTO;
import com.aisolutions.vendormanagement.entity.VendorInvSubmission;
import com.aisolutions.vendormanagement.entity.VendorInvSubmissionDetail;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@ApplicationScoped
@WithSession
public class VendorInvSubmissionRepository implements PanacheRepositoryBase<VendorInvSubmission, Long> {

  // #region SELECT clauses

  private static final String INV_SELECT = """
      SELECT new com.aisolutions.vendormanagement.dto.VendorInvSubmissionDTO(
          i.uniqId, i.entryStaff, i.entryDate, i.lastEditStaff, i.lastEditDate,
          i.invoiceStatus, i.invoiceNumber, i.invoiceDate, i.terms, i.termsDay,
          i.invoiceDueDate, i.contactType, i.vendorId, i.vendorName, i.vendorInvoice,
          i.vendorAttentionTo, i.currency, i.exchangeRate, i.subTotalForeign,
          i.taxForeign, i.totalForeign, i.subTotalBase, i.taxBase, i.totalBase,
          i.remarks, i.exchangeRateDate, i.balanceAmt, i.projectCode, i.referenceOur,
          i.inChargeStaff, i.paymentVoucher, i.assignedStaff
      )
      FROM VendorInvSubmission i
      """;

  private static final String INV_DET_SELECT = """
      SELECT new com.aisolutions.vendormanagement.dto.VendorInvSubmissionDetailDTO(
          d.uniqId, d.entryStaff, d.entryDate, d.lastEditStaff, d.lastEditDate,
          d.invoiceNumber, d.detailCode, d.referenceId, d.ledgerCode, d.description,
          d.taxType, d.taxRate, d.currency, d.exchangeRate, d.quantity,
          d.subTotalForeign, d.taxForeign, d.totalForeign, d.subTotalBase,
          d.taxBase, d.totalBase
      )
      FROM VendorInvSubmissionDetail d
      """;

  private static final String PO_SELECT = """
      SELECT new com.aisolutions.vendormanagement.dto.PurchaseOrderDTO(
          p.uniqId, p.entryStaff, p.entryDate, p.lastEditStaff, p.lastEditDate,
          p.poStatus, p.poNumber, p.poDate, p.requireDate, p.dueDate,
          p.referenceOur, p.referenceYour, p.terms, p.termsDay, p.contactType,
          p.supplierId, p.supplierName, p.supplierAddress1, p.supplierAddress2,
          p.supplierAddress3, p.deliveryAddress1, p.deliveryAddress2, p.deliveryAddress3,
          p.currency, p.exchangeRate, p.inChargeStaffId, p.totalForeign, p.remarks
      )
      FROM PurchaseOrder p
      """;

  private static final String PO_DET_SELECT = """
      SELECT new com.aisolutions.vendormanagement.dto.PurchaseOrderDetailDTO(
          d.uniqId, d.entryStaff, d.entryDate, d.lastEditStaff, d.lastEditDate,
          d.poNumber, d.opCode, d.mainRefCode, d.refType, d.billStatus, d.deliverStatus,
          d.costCode, d.itemCode, d.detailCode, d.sequence, d.description, d.comments,
          d.currency, d.quantityOrder, d.quantityIn, d.quantityBill, d.unitMs,
          d.unitMsForeign, d.subTotalForeign, d.totalBill, d.ledgerCode, d.ledgerName,
          d.subType, d.discPct, d.subContractId, d.partNo, d.source, d.vendPartNo,
          d.staffId, d.groupId, d.detailId, d.grpDetName, d.subLedgerCode, d.prNo, d.prUniqId,
          d.prRequiredDate, d.supplierPartNo, d.cutOffQtyIn, d.cufOffGRNOValue, d.cutOffGRNValue,
          d.grnQuantityIn, d.requestedBy, d.refSQLCode, d.refDocNo, d.noOfCarton
      )
      FROM PurchaseOrderDetail d
      """;

  // #endregion

   // #region Purchase Order Queries (for invoice creation)
 
    /**
     * Fetch PO by ID with vendor ownership check
     */
    public Uni<PurchaseOrderDTO> fetchPurchaseOrderById(Long id, String vendorId) {
        return getSession().flatMap(session ->
            session.createSelectionQuery(
                PO_SELECT +
                "WHERE p.uniqId = :id AND p.supplierId = :vendorId",
                PurchaseOrderDTO.class
            )
            .setParameter("id", id)
            .setParameter("vendorId", vendorId)
            .getSingleResultOrNull()
        );
    }
 
    /**
     * Fetch PO details by PO number
     */
    public Uni<List<PurchaseOrderDetailDTO>> fetchPurchaseOrderDetails(String poNumber) {
        return getSession().flatMap(session ->
            session.createSelectionQuery(
                PO_DET_SELECT +
                "WHERE d.poNumber = :poNumber " +
                "ORDER BY d.sequence",
                PurchaseOrderDetailDTO.class
            )
            .setParameter("poNumber", poNumber)
            .getResultList()
        );
    }
 
    // #endregion

  // #region Invoice Header Queries

  /**
   * Fetch all invoices submitted by a vendor (by vendorId)
   */
  public Uni<List<VendorInvSubmissionDTO>> fetchInvoicesByVendorId(String vendorId) {
    return getSession().flatMap(session -> session.createQuery(
        INV_SELECT +
            "WHERE i.vendorId = :vendorId " +
            "ORDER BY i.invoiceDate DESC",
        VendorInvSubmissionDTO.class)
        .setParameter("vendorId", vendorId)
        .getResultList());
  }

  /**
   * Fetch a single invoice by ID with vendor ownership check
   */
  public Uni<VendorInvSubmissionDTO> fetchInvoiceById(Long id, String vendorId) {
    return getSession().flatMap(session -> session.createQuery(
        INV_SELECT +
            "WHERE i.uniqId = :id AND i.vendorId = :vendorId",
        VendorInvSubmissionDTO.class)
        .setParameter("id", id)
        .setParameter("vendorId", vendorId)
        .getSingleResultOrNull());
  }

  /**
   * Fetch invoices by status
   */
  public Uni<List<VendorInvSubmissionDTO>> fetchInvoicesByStatus(String vendorId, String status) {
    return getSession().flatMap(session -> session.createQuery(
        INV_SELECT +
            "WHERE i.vendorId = :vendorId AND i.invoiceStatus = :status " +
            "ORDER BY i.invoiceDate DESC",
        VendorInvSubmissionDTO.class)
        .setParameter("vendorId", vendorId)
        .setParameter("status", status)
        .getResultList());
  }

  // #endregion

  // #region Invoice Detail Queries

  /**
   * Fetch invoice line items by invoice number
   */
  public Uni<List<VendorInvSubmissionDetailDTO>> fetchInvoiceDetails(String invoiceNumber) {
    return getSession().flatMap(session -> session.createQuery(
        INV_DET_SELECT +
            "WHERE d.invoiceNumber = :invoiceNumber " +
            "ORDER BY d.detailCode",
        VendorInvSubmissionDetailDTO.class)
        .setParameter("invoiceNumber", invoiceNumber)
        .getResultList());
  }

  // #endregion

  // #region Insert/Update

  /**
   * Insert a new invoice submission
   */
  public Uni<VendorInvSubmission> insertInvoice(VendorInvSubmission invoice) {
    return getSession().flatMap(session -> session.persist(invoice)
        .replaceWith(invoice));
  }

  /**
   * Insert invoice detail line
   */
  public Uni<VendorInvSubmissionDetail> insertInvoiceDetail(VendorInvSubmissionDetail detail) {
    return getSession().flatMap(session -> session.persist(detail)
        .replaceWith(detail));
  }

  /**
   * Update invoice status
   */
  public Uni<Integer> updateInvoiceStatus(Long id, String status, String lastEditStaff) {
    return getSession().flatMap(session -> session.createMutationQuery("""
        UPDATE VendorInvSubmission i
        SET i.invoiceStatus = :status,
            i.lastEditStaff = :lastEditStaff,
            i.lastEditDate = CURRENT_TIMESTAMP
        WHERE i.uniqId = :id
        """)
        .setParameter("id", id)
        .setParameter("status", status)
        .setParameter("lastEditStaff", lastEditStaff)
        .executeUpdate());
  }

  /**
   * Generate next invoice number (global sequence)
   * Format: INV-{YYMM}-{SEQ}
   * Example: INV-2604-0001 (13 chars)
   */
  public Uni<String> generateInvoiceNumber(String vendorId) {
      String prefix = "INV-" + java.time.LocalDate.now().format(
          java.time.format.DateTimeFormatter.ofPattern("yyMM")
      ) + "-";
      
      return getSession().flatMap(session ->
          session.createSelectionQuery("""
              SELECT MAX(i.invoiceNumber)
              FROM VendorInvSubmission i
              WHERE i.invoiceNumber LIKE :prefix
              """, String.class)
          .setParameter("prefix", prefix + "%")
          .getSingleResultOrNull()
      ).map(lastInvoice -> {
          int seq = 1;
          if (lastInvoice != null) {
              String seqPart = lastInvoice.substring(lastInvoice.lastIndexOf("-") + 1);
              seq = Integer.parseInt(seqPart) + 1;
          }
          return prefix + String.format("%04d", seq);
      });
  }

  // #endregion

  // #region Dashboard Stats

  /**
   * Count invoices by status for vendor dashboard
   */
  public Uni<Long> countByStatus(String vendorId, String status) {
    return getSession().flatMap(session -> session.createQuery("""
        SELECT COUNT(i) FROM VendorInvSubmission i
        WHERE i.vendorId = :vendorId AND i.invoiceStatus = :status
        """, Long.class)
        .setParameter("vendorId", vendorId)
        .setParameter("status", status)
        .getSingleResult());
  }

  /**
   * Get total pending amount for vendor
   */
  public Uni<java.math.BigDecimal> getTotalPendingAmount(String vendorId) {
    return getSession().flatMap(session -> session.createQuery("""
        SELECT COALESCE(SUM(i.totalForeign), 0)
        FROM VendorInvSubmission i
        WHERE i.vendorId = :vendorId AND i.invoiceStatus IN ('OPEN', 'PENDING')
        """, java.math.BigDecimal.class)
        .setParameter("vendorId", vendorId)
        .getSingleResult());
  }

  // #endregion
}