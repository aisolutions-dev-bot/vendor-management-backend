package com.aisolutions.vendormanagement.service.attachment;

import com.aisolutions.vendormanagement.dto.AttachmentDTO;
import com.aisolutions.vendormanagement.entity.Attachment;
import com.aisolutions.vendormanagement.repository.AttachmentRepository;
import com.aisolutions.vendormanagement.service.CurrentUserService;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class AttachmentService {

  @Inject
  AttachmentRepository attachmentRepository;

  @Inject
  CurrentUserService currentUserService;

  private static final List<String> ALLOWED_EXTENSIONS = List.of(
    ".pdf", ".doc", ".docx", ".xls", ".xlsx", 
    ".jpg", ".jpeg", ".png", ".gif", ".txt"
  );

  private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

  // #region GET METHODS

  /**
   * Get all attachments for a module/reference
   */
  public Uni<List<AttachmentDTO>> getAttachments(String moduleType, String referenceCode) {
    if (moduleType == null || moduleType.isBlank()) {
      return Uni.createFrom().failure(
          new IllegalArgumentException("Module type is required")
      );
    }
    if (referenceCode == null || referenceCode.isBlank()) {
      return Uni.createFrom().failure(
          new IllegalArgumentException("Reference code is required")
      );
    }

    return attachmentRepository.findByModuleAndReference(
      moduleType.toUpperCase(), 
      referenceCode.toUpperCase()
    );
  }

  /**
   * Get attachment by ID
   */
  public Uni<Attachment> getAttachmentById(Long uniqId) {
    return attachmentRepository.findByIdWithoutData(uniqId);
  }

  // #endregion

  //#region DOWNLOAD METHODS

    /**
     * Download file content (supports both FTP and legacy LOCAL storage)
     */
    public Uni<byte[]> downloadFile(Long uniqId) {
        if (uniqId == null) {
            return Uni.createFrom().failure(
                new IllegalArgumentException("Attachment ID is required")
            );
        }

        return attachmentRepository.downloadFileContent(uniqId);
    }

    /**
     * Get attachment metadata for download (without file data)
     */
    public Uni<Attachment> getAttachmentForDownload(Long uniqId) {
        return attachmentRepository.findByIdWithoutData(uniqId);
    }

    //#endregion

  // #region UPLOAD METHODS

  /**
   * Upload single file
   */
  public Uni<AttachmentDTO> uploadFile(
    String moduleType,
    String referenceCode,
    String originalName,
    String contentType,
    byte[] fileData) {

    // Validate inputs
    String validationError = validateUpload(moduleType, referenceCode, originalName, fileData);
    if (validationError != null) {
      return Uni.createFrom().failure(new IllegalArgumentException(validationError));
    }

    return currentUserService.getCurrentUser()
      .onItem().transformToUni(currentUser ->

        Panache.withTransaction(() -> 
          attachmentRepository.createAttachment(
            moduleType,
            referenceCode,
            originalName,
            contentType,
            (long) fileData.length,
            fileData,
            currentUser.getStaffId()
          )
        ) 
      )
      .onItem().transform(this::mapEntityToDTO);
  }

  /**
   * Validate upload parameters
   */
  private String validateUpload(
      String moduleType,
      String referenceCode,
      String originalName,
      byte[] fileData) {

    if (moduleType == null || moduleType.isBlank()) {
      return "Module type is required";
    }
    if (referenceCode == null || referenceCode.isBlank()) {
      return "Reference code is required";
    }
    if (originalName == null || originalName.isBlank()) {
      return "File name is required";
    }
    if (fileData == null || fileData.length == 0) {
      return "File data is empty";
    }

    // Validate file extension
    String extension = getFileExtension(originalName).toLowerCase();
    if (!ALLOWED_EXTENSIONS.contains(extension)) {
      return "File type not allowed: " + extension + 
        ". Allowed types: " + String.join(", ", ALLOWED_EXTENSIONS);
    }

    // Validate file size
    if (fileData.length > MAX_FILE_SIZE) {
      return "File size exceeds maximum of " + (MAX_FILE_SIZE / 1024 / 1024) + "MB";
    }

    return null; 
  }

  // #endregion

  // #region DELETE METHODS

  /**
   * Delete attachment
   */
  public Uni<Boolean> deleteAttachment(Long uniqId) {
    if (uniqId == null) {
      return Uni.createFrom().failure(
        new IllegalArgumentException("Attachment ID is required")
      );
    }

    return Panache.withTransaction(() -> 
      attachmentRepository.deleteAttachment(uniqId)
    );
  }

  // #endregion

  // #region HELPER METHODS

  private String getFileExtension(String filename) {
    if (filename == null || filename.isBlank()) {
      return "";
    }
    int lastDot = filename.lastIndexOf('.');
    return lastDot != -1 ? filename.substring(lastDot) : "";
  }

  private AttachmentDTO mapEntityToDTO(Attachment entity) {
    return new AttachmentDTO(
      entity.getUniqId(),
      entity.getModuleType(),
      entity.getReferenceCode(),
      entity.getFileName(),
      entity.getOriginalName(),
      entity.getFileSize(),
      entity.getStorageType(),
      entity.getContentType(),
      entity.getFileExtension(),
      entity.getFilePath(),
      entity.getDescription(),
      entity.getUploadSource(),
      entity.getEntryStaff(),
      entity.getEntryDate()
    );
  }

  // #endregion
}