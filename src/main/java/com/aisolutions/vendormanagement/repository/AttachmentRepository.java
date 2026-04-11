package com.aisolutions.vendormanagement.repository;

import com.aisolutions.vendormanagement.dto.AttachmentDTO;
import com.aisolutions.vendormanagement.entity.Attachment;
import com.aisolutions.vendormanagement.service.attachment.FTPStorageService;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
@WithSession
public class AttachmentRepository implements PanacheRepositoryBase<Attachment, Long> {

  @Inject
  FTPStorageService ftpStorageService;

  // #region GET METHODS

  /**
   * Get all attachments for a module/reference (without file data)
   */
  public Uni<List<AttachmentDTO>> findByModuleAndReference(String moduleType, String referenceCode) {
    return getSession().flatMap(session -> 
      session.createQuery(
        "SELECT new com.aisolutions.vendormanagement.dto.AttachmentDTO(" +
            "a.uniqId, a.moduleType, a.referenceCode, a.fileName, a.originalName, " +
            "a.fileSize, a.storageType, a.contentType, a.fileExtension, a.filePath, " +
            "a.description, a.uploadSource, a.entryStaff, a.entryDate) " +
            "FROM Attachment a " +
            "WHERE a.moduleType = :moduleType AND a.referenceCode = :referenceCode " +
            "ORDER BY a.entryDate DESC",
        AttachmentDTO.class)
        .setParameter("moduleType", moduleType)
        .setParameter("referenceCode", referenceCode)
        .getResultList()
      )
      .onFailure().invoke(e -> {
        System.err.println("Error fetching attachments: " + e.getMessage());
        e.printStackTrace();
      });
  }

  /**
   * Get attachment by ID (metadata only, no file data)
   */
  public Uni<Attachment> findByIdWithoutData(Long uniqId) {
    return getSession().flatMap(session -> 
      session.find(Attachment.class, uniqId)
    );
  }

  /**
   * Get attachment by ID (with file data for download - legacy LOCAL storage)
   */
  public Uni<Attachment> findByIdWithData(Long uniqId) {
    return getSession().flatMap(session -> 
      session.find(Attachment.class, uniqId)
    );
  }

  /**
   * Get attachment count for a module/reference
   */
  public Uni<Long> countByModuleAndReference(String moduleType, String referenceCode) {
    return getSession().flatMap(session -> session.createQuery(
        "SELECT COUNT(a) FROM Attachment a " +
            "WHERE a.moduleType = :moduleType AND a.referenceCode = :referenceCode",
        Long.class)
        .setParameter("moduleType", moduleType)
        .setParameter("referenceCode", referenceCode)
        .getSingleResult());
  }

  // #endregion

  // #region CREATE METHODS

  /**
   * Save new attachment
   */
  public Uni<Attachment> createAttachment(
      String moduleType,
      String referenceCode,
      String originalName,
      String contentType,
      Long fileSize,
      byte[] fileData,
      String currentUser) {

    // Upload first to FTP, then save metadata to database
    return ftpStorageService.uploadFile(fileData, moduleType, referenceCode, originalName)
        .flatMap(remotePath -> {
          // Save metadata after file successfully upload
          return getSession().flatMap(session -> {
            Attachment entity = new Attachment();

            // Generate unique filename
            String extension = getFileExtension(originalName);
            String uniqueFileName = UUID.randomUUID().toString() + extension;

            entity.setModuleType(moduleType.toUpperCase());
            entity.setReferenceCode(referenceCode.toUpperCase());
            entity.setFileName(uniqueFileName);
            entity.setOriginalName(originalName);
            entity.setFileSize(fileSize);
            entity.setContentType(contentType);
            entity.setFileExtension(extension);

            // FTP storage - no binary data in database
            entity.setStorageType("FTP");
            entity.setFilePath(remotePath);
            entity.setFileData(null); // Not store the file data in database

            entity.setUploadSource("WEB");

            // Audit fields
            entity.setEntryStaff(currentUser == null ? null : currentUser);
            entity.setEntryDate(LocalDateTime.now());

            return session.persist(entity).replaceWith(entity);
          });
        })
        .onFailure().invoke(e -> {
          System.err.println("Error creating attachment: " + e.getMessage());
          e.printStackTrace();
        });
  }

  // #endregion

  // #region DOWNLOAD METHODS

  /**
   * Download file content - handles both FTP and legacy LOCAL storage
   */
  public Uni<byte[]> downloadFileContent(Long uniqId) {
    return findByIdWithoutData(uniqId)
        .flatMap(attachment -> {
          if (attachment == null) {
            return Uni.createFrom().failure(
                new RuntimeException("Attachment not found: " + uniqId));
          }

          String storageType = attachment.getStorageType();

          if ("FTP".equalsIgnoreCase(storageType)) {
            // Download from FTP server
            String filePath = attachment.getFilePath();
            if (filePath == null || filePath.isBlank()) {
              return Uni.createFrom().failure(
                  new RuntimeException("File path not found for attachment: " + uniqId));
            }
            return ftpStorageService.downloadFile(filePath);

          } else if ("LOCAL".equalsIgnoreCase(storageType)) {
            // Legacy: Get from database (for old records)
            byte[] fileData = attachment.getFileData();
            if (fileData == null) {
              return Uni.createFrom().failure(
                  new RuntimeException("File data not found in database: " + uniqId));
            }
            return Uni.createFrom().item(fileData);

          } else {
            return Uni.createFrom().failure(
                new RuntimeException("Unknown storage type: " + storageType));
          }
        });
  }

  // #endregion

  // #region DELETE METHODS

  /**
   * Delete attachment - removes from FTP and database
   */
  public Uni<Boolean> deleteAttachment(Long uniqId) {
    return findByIdWithoutData(uniqId)
      .flatMap(attachment -> {
        if (attachment == null) {
          return Uni.createFrom().item(false);
        }

        String storageType = attachment.getStorageType();
        String filePath = attachment.getFilePath();

        // Delete from FTP storage first (if applicable)
        Uni<Boolean> deleteFromStorage;

        if ("FTP".equalsIgnoreCase(storageType) && filePath != null && !filePath.isBlank()) {
          deleteFromStorage = ftpStorageService.deleteFile(filePath);
        } else {
          deleteFromStorage = Uni.createFrom().item(true);
        }

        // Then delete from database
        return deleteFromStorage
            .flatMap(deleted -> getSession().flatMap(session -> session.find(Attachment.class, uniqId)
                .onItem().ifNotNull().transformToUni(entity -> session.remove(entity).replaceWith(true))
                .onItem().ifNull().continueWith(false)));
      })
      .onFailure().invoke(e -> {
        System.err.println("Error deleting attachment: " + e.getMessage());
        e.printStackTrace();
      });
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

  // #endregion
}