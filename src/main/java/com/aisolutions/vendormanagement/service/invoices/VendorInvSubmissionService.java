package com.aisolutions.vendormanagement.service.invoices;

import com.aisolutions.vendormanagement.dto.CreateInvoiceRequestDTO;
import com.aisolutions.vendormanagement.dto.PurchaseOrderDetailDTO;
import com.aisolutions.vendormanagement.dto.VendorInvSubmissionDTO;
import com.aisolutions.vendormanagement.dto.VendorInvSubmissionDetailDTO;
import com.aisolutions.vendormanagement.entity.VendorInvSubmission;
import com.aisolutions.vendormanagement.entity.VendorInvSubmissionDetail;
import com.aisolutions.vendormanagement.repository.PurchaseOrderRepository;
import com.aisolutions.vendormanagement.repository.VendorInvSubmissionRepository;
import com.aisolutions.vendormanagement.service.CurrentUserService;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@ApplicationScoped
public class VendorInvSubmissionService {

  @Inject
  VendorInvSubmissionRepository invoiceRepository;

  @Inject
  PurchaseOrderRepository purchaseOrderRepository;

  @Inject
  CurrentUserService currentUserService;

  // TODO: Remove hardcode after vendor auth is implemented
  private static final String HARDCODED_VENDOR_ID = "EVECONPL01";

  // #region Get Invoices

  /**
   * Get all invoices for the logged-in vendor
   */
  public Uni<List<VendorInvSubmissionDTO>> getInvoices() {
    // TODO: Remove hardcode after vendor auth is implemented
    String vendorId = HARDCODED_VENDOR_ID;
    log.info("Fetching invoices for vendor: {}", vendorId);
    return invoiceRepository.fetchInvoicesByVendorId(vendorId);
    // return currentUserService.getCurrentUserLoginId()
    // .onItem().transformToUni(vendorId -> {
    // if (vendorId == null || vendorId.isBlank()) {
    // return Uni.createFrom().failure(
    // new IllegalStateException("Unable to determine logged-in vendor"));
    // }
    // return invoiceRepository.fetchInvoicesByVendorId(vendorId);
    // });
  }

  /**
   * Get invoices filtered by status
   */
  public Uni<List<VendorInvSubmissionDTO>> getInvoicesByStatus(String status) {
    // TODO: Remove hardcode after vendor auth is implemented
    String vendorId = HARDCODED_VENDOR_ID;
    log.info("Fetching invoices for vendor: {} with status: {}", vendorId, status);
    return invoiceRepository.fetchInvoicesByStatus(vendorId, status);
    // return currentUserService.getCurrentUserLoginId()
    // .onItem().transformToUni(vendorId -> {
    // if (vendorId == null || vendorId.isBlank()) {
    // return Uni.createFrom().failure(
    // new IllegalStateException("Unable to determine logged-in vendor"));
    // }
    // return invoiceRepository.fetchInvoicesByStatus(vendorId, status);
    // });
  }

  /**
   * Get a single invoice by ID with ownership validation
   */
  public Uni<VendorInvSubmissionDTO> getInvoiceById(Long id) {
    // TODO: Remove hardcode after vendor auth is implemented
    String vendorId = HARDCODED_VENDOR_ID;
    return invoiceRepository.fetchInvoiceById(id, vendorId)
        .onItem().ifNull().failWith(() -> new IllegalArgumentException("Invoice not found"));
    // return currentUserService.getCurrentUserLoginId()
    // .onItem().transformToUni(vendorId -> {
    // if (vendorId == null || vendorId.isBlank()) {
    // return Uni.createFrom().failure(
    // new IllegalStateException("Unable to determine logged-in vendor"));
    // }
    // return invoiceRepository.fetchInvoiceById(id, vendorId)
    // .onItem().ifNull().failWith(() -> new IllegalArgumentException("Invoice not
    // found"));
    // });
  }

  /**
   * Get invoice line items
   */
  public Uni<List<VendorInvSubmissionDetailDTO>> getInvoiceDetails(Long invoiceId) {
    // First verify vendor owns this invoice, then fetch details
    return getInvoiceById(invoiceId)
        .onItem().transformToUni(invoice -> invoiceRepository.fetchInvoiceDetails(invoice.getInvoiceNumber()));
  }

  // #endregion

  // #region Create Invoice

  /**
   * Create a new invoice submission from Purchase Order.
   * Fetches PO data and creates invoice with line items from PO details.
   */
  public Uni<VendorInvSubmissionDTO> createInvoiceFromPO(CreateInvoiceRequestDTO request) {
    // TODO: Remove hardcode after vendor auth is implemented
    String vendorId = HARDCODED_VENDOR_ID;

    // Validate request
    if (request == null || request.getPoUniqId() == null) {
      return Uni.createFrom().failure(
          new IllegalArgumentException("PO ID is required"));
    }

    log.info("Creating invoice from PO {} for vendor {}", request.getPoUniqId(), vendorId);

    return Panache.withTransaction(() -> {
      // First verify PO exists and belongs to vendor, then fetch details
      return purchaseOrderRepository.fetchPurchaseOrderById(request.getPoUniqId(), vendorId)
          .onItem().ifNull().failWith(() -> new IllegalArgumentException("Purchase Order not found or access denied"))
          .onItem().transformToUni(po -> purchaseOrderRepository.fetchPurchaseOrderDetails(po.getPoNumber())
              .onItem().transformToUni(poDetails ->
              // Generate invoice number and create invoice
              invoiceRepository.generateInvoiceNumber(vendorId)
                  .onItem().transformToUni(invoiceNumber -> {
                    log.info("Generated invoice number: {}", invoiceNumber);

                    // Create header entity from PO data
                    VendorInvSubmission invoice = new VendorInvSubmission();
                    invoice.setInvoiceNumber(invoiceNumber);
                    invoice.setInvoiceStatus("SUBMIT");
                    invoice.setInvoiceDate(LocalDateTime.now());
                    invoice.setVendorId(vendorId);
                    invoice.setVendorName(po.getSupplierName());
                    invoice.setCurrency(request.getCurrency() != null ? request.getCurrency() : po.getCurrency());
                    invoice.setExchangeRate(
                        request.getExchangeRate() != null ? request.getExchangeRate() : po.getExchangeRate());
                    invoice.setReferenceOur(po.getPoNumber());
                    invoice.setProjectCode(po.getReferenceOur()); // Project reference from PO
                    invoice.setTerms(po.getTerms());
                    invoice.setTermsDay(po.getTermsDay() != null ? po.getTermsDay().intValue() : null);
                    invoice.setRemarks("Created from PO: " + po.getPoNumber());

                    // Calculate totals from PO details
                    BigDecimal subTotal = request.getTotalForeign() != null
                        ? request.getTotalForeign()
                        : po.getTotalForeign();
                    invoice.setSubTotalForeign(subTotal);
                    invoice.setTaxForeign(BigDecimal.ZERO);
                    invoice.setTotalForeign(subTotal);
                    invoice.setBalanceAmt(subTotal);

                    // Calculate base amounts
                    BigDecimal rate = invoice.getExchangeRate() != null ? invoice.getExchangeRate() : BigDecimal.ONE;
                    invoice.setSubTotalBase(subTotal.multiply(rate));
                    invoice.setTaxBase(BigDecimal.ZERO);
                    invoice.setTotalBase(subTotal.multiply(rate));

                    // Calculate due date
                    if (po.getTermsDay() != null && po.getTermsDay().compareTo(BigDecimal.ZERO) > 0) {
                      invoice.setInvoiceDueDate(LocalDateTime.now().plusDays(po.getTermsDay().longValue()));
                    }

                    // Audit fields
                    invoice.setEntryStaff(vendorId);
                    invoice.setEntryDate(LocalDateTime.now());

                    return invoiceRepository.insertInvoice(invoice)
                        .onItem().transformToUni(savedInvoice -> {
                          // Create detail entities from PO details
                          if (poDetails == null || poDetails.isEmpty()) {
                            log.info("No PO details found, returning invoice without line items");
                            return Uni.createFrom().item(mapEntityToDto(savedInvoice));
                          }

                          log.info("Creating {} invoice detail lines from PO", poDetails.size());

                          // Insert all details sequentially
                          Uni<Void> insertChain = Uni.createFrom().voidItem();
                          int seq = 1;
                          for (PurchaseOrderDetailDTO poDetail : poDetails) {
                            VendorInvSubmissionDetail det = new VendorInvSubmissionDetail();
                            det.setInvoiceNumber(invoiceNumber);
                            det.setDetailCode(String.format("%04d", seq++));
                            det.setReferenceId(po.getPoNumber());
                            det.setDescription(poDetail.getDescription());
                            det.setCurrency(invoice.getCurrency());
                            det.setExchangeRate(invoice.getExchangeRate());
                            det.setQuantity(poDetail.getQuantityOrder());
                            det.setSubTotalForeign(poDetail.getSubTotalForeign());
                            det.setTaxForeign(BigDecimal.ZERO);
                            det.setTotalForeign(poDetail.getSubTotalForeign());
                            det.setEntryStaff(vendorId);
                            det.setEntryDate(LocalDateTime.now());

                            final VendorInvSubmissionDetail finalDet = det;
                            insertChain = insertChain.chain(() -> invoiceRepository.insertInvoiceDetail(finalDet)
                                .replaceWithVoid());
                          }

                          return insertChain.replaceWith(mapEntityToDto(savedInvoice));
                        });
                  })));
    });
  }

  /**
   * Create a new invoice submission with line items (legacy method for full DTO)
   */
  public Uni<VendorInvSubmissionDTO> createInvoice(
      VendorInvSubmissionDTO headerDto,
      List<VendorInvSubmissionDetailDTO> details) {

    // Validate header
    if (headerDto == null) {
      return Uni.createFrom().failure(
          new IllegalArgumentException("Invoice header is required"));
    }

    // TODO: Remove hardcode after vendor auth is implemented
    String vendorId = HARDCODED_VENDOR_ID;
    return Panache.withTransaction(() -> {
      return invoiceRepository.generateInvoiceNumber(vendorId)
          .onItem().transformToUni(invoiceNumber -> {
            // Create header entity
            VendorInvSubmission invoice = new VendorInvSubmission();
            mapDtoToEntity(headerDto, invoice);
            invoice.setInvoiceNumber(invoiceNumber);
            invoice.setVendorId(vendorId);
            invoice.setInvoiceStatus("SUBMIT");
            invoice.setEntryStaff(vendorId);
            invoice.setEntryDate(LocalDateTime.now());

            return invoiceRepository.insertInvoice(invoice)
                .onItem().transformToUni(savedInvoice -> {
                  // Create detail entities
                  if (details == null || details.isEmpty()) {
                    return Uni.createFrom().item(mapEntityToDto(savedInvoice));
                  }

                  // Insert all details sequentially
                  Uni<Void> insertChain = Uni.createFrom().voidItem();
                  int seq = 1;
                  for (VendorInvSubmissionDetailDTO detDto : details) {
                    VendorInvSubmissionDetail det = new VendorInvSubmissionDetail();
                    mapDetailDtoToEntity(detDto, det);
                    det.setInvoiceNumber(invoiceNumber);
                    det.setDetailCode(String.format("%04d", seq++));
                    det.setEntryStaff(vendorId);
                    det.setEntryDate(LocalDateTime.now());

                    final VendorInvSubmissionDetail finalDet = det;
                    insertChain = insertChain.chain(() -> invoiceRepository.insertInvoiceDetail(finalDet)
                        .replaceWithVoid());
                  }

                  return insertChain.replaceWith(mapEntityToDto(savedInvoice));
                });
          });
    });
  }

  // #endregion

  // #region Dashboard Stats

  /**
   * Get dashboard statistics for the logged-in vendor
   */
  public Uni<Map<String, Object>> getDashboardStats() {
    // TODO: Remove hardcode after vendor auth is implemented
    String vendorId = HARDCODED_VENDOR_ID;
    Uni<Long> openCount = invoiceRepository.countByStatus(vendorId, "OPEN");
    Uni<Long> pendingCount = invoiceRepository.countByStatus(vendorId, "PENDING");
    Uni<Long> approvedCount = invoiceRepository.countByStatus(vendorId, "APPROVED");
    Uni<BigDecimal> totalPending = invoiceRepository.getTotalPendingAmount(vendorId);

    return Uni.combine().all().unis(openCount, pendingCount, approvedCount, totalPending)
        .asTuple()
        .map(tuple -> Map.of(
            "openInvoices", tuple.getItem1(),
            "pendingInvoices", tuple.getItem2(),
            "approvedInvoices", tuple.getItem3(),
            "totalPendingAmount", tuple.getItem4()));
    // return currentUserService.getCurrentUserLoginId()
    // .onItem().transformToUni(vendorId -> {
    // if (vendorId == null || vendorId.isBlank()) {
    // return Uni.createFrom().failure(
    // new IllegalStateException("Unable to determine logged-in vendor"));
    // }
    // Uni<Long> openCount = invoiceRepository.countByStatus(vendorId, "OPEN");
    // Uni<Long> pendingCount = invoiceRepository.countByStatus(vendorId,
    // "PENDING");
    // Uni<Long> approvedCount = invoiceRepository.countByStatus(vendorId,
    // "APPROVED");
    // Uni<BigDecimal> totalPending =
    // invoiceRepository.getTotalPendingAmount(vendorId);
    // return Uni.combine().all().unis(openCount, pendingCount, approvedCount,
    // totalPending)
    // .asTuple()
    // .map(tuple -> Map.of(
    // "openInvoices", tuple.getItem1(),
    // "pendingInvoices", tuple.getItem2(),
    // "approvedInvoices", tuple.getItem3(),
    // "totalPendingAmount", tuple.getItem4()));
    // });
  }

  // #endregion

  // #region Mapping Helpers

  private void mapDtoToEntity(VendorInvSubmissionDTO dto, VendorInvSubmission entity) {
    entity.setInvoiceDate(dto.getInvoiceDate());
    entity.setTerms(dto.getTerms());
    entity.setTermsDay(dto.getTermsDay());
    entity.setInvoiceDueDate(dto.getInvoiceDueDate());
    entity.setContactType(dto.getContactType());
    entity.setVendorName(dto.getVendorName());
    entity.setVendorInvoice(dto.getVendorInvoice());
    entity.setVendorAttentionTo(dto.getVendorAttentionTo());
    entity.setCurrency(dto.getCurrency());
    entity.setExchangeRate(dto.getExchangeRate());
    entity.setSubTotalForeign(dto.getSubTotalForeign());
    entity.setTaxForeign(dto.getTaxForeign());
    entity.setTotalForeign(dto.getTotalForeign());
    entity.setSubTotalBase(dto.getSubTotalBase());
    entity.setTaxBase(dto.getTaxBase());
    entity.setTotalBase(dto.getTotalBase());
    entity.setRemarks(dto.getRemarks());
    entity.setExchangeRateDate(dto.getExchangeRateDate());
    entity.setProjectCode(dto.getProjectCode());
    entity.setReferenceOur(dto.getReferenceOur());
  }

  private VendorInvSubmissionDTO mapEntityToDto(VendorInvSubmission entity) {
    VendorInvSubmissionDTO dto = new VendorInvSubmissionDTO();
    dto.setUniqId(entity.getUniqId());
    dto.setEntryStaff(entity.getEntryStaff());
    dto.setEntryDate(entity.getEntryDate());
    dto.setLastEditStaff(entity.getLastEditStaff());
    dto.setLastEditDate(entity.getLastEditDate());
    dto.setInvoiceStatus(entity.getInvoiceStatus());
    dto.setInvoiceNumber(entity.getInvoiceNumber());
    dto.setInvoiceDate(entity.getInvoiceDate());
    dto.setTerms(entity.getTerms());
    dto.setTermsDay(entity.getTermsDay());
    dto.setInvoiceDueDate(entity.getInvoiceDueDate());
    dto.setContactType(entity.getContactType());
    dto.setVendorId(entity.getVendorId());
    dto.setVendorName(entity.getVendorName());
    dto.setVendorInvoice(entity.getVendorInvoice());
    dto.setVendorAttentionTo(entity.getVendorAttentionTo());
    dto.setCurrency(entity.getCurrency());
    dto.setExchangeRate(entity.getExchangeRate());
    dto.setSubTotalForeign(entity.getSubTotalForeign());
    dto.setTaxForeign(entity.getTaxForeign());
    dto.setTotalForeign(entity.getTotalForeign());
    dto.setSubTotalBase(entity.getSubTotalBase());
    dto.setTaxBase(entity.getTaxBase());
    dto.setTotalBase(entity.getTotalBase());
    dto.setRemarks(entity.getRemarks());
    dto.setExchangeRateDate(entity.getExchangeRateDate());
    dto.setBalanceAmt(entity.getBalanceAmt());
    dto.setProjectCode(entity.getProjectCode());
    dto.setReferenceOur(entity.getReferenceOur());
    dto.setInChargeStaff(entity.getInChargeStaff());
    dto.setPaymentVoucher(entity.getPaymentVoucher());
    dto.setAssignedStaff(entity.getAssignedStaff());
    return dto;
  }

  private void mapDetailDtoToEntity(VendorInvSubmissionDetailDTO dto, VendorInvSubmissionDetail entity) {
    entity.setReferenceId(dto.getReferenceId());
    entity.setLedgerCode(dto.getLedgerCode());
    entity.setDescription(dto.getDescription());
    entity.setTaxType(dto.getTaxType());
    entity.setTaxRate(dto.getTaxRate());
    entity.setCurrency(dto.getCurrency());
    entity.setExchangeRate(dto.getExchangeRate());
    entity.setQuantity(dto.getQuantity());
    entity.setSubTotalForeign(dto.getSubTotalForeign());
    entity.setTaxForeign(dto.getTaxForeign());
    entity.setTotalForeign(dto.getTotalForeign());
    entity.setSubTotalBase(dto.getSubTotalBase());
    entity.setTaxBase(dto.getTaxBase());
    entity.setTotalBase(dto.getTotalBase());
  }

  // #endregion
}