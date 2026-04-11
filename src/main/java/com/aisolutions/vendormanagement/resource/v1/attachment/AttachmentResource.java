package com.aisolutions.vendormanagement.resource.v1.attachment;

import com.aisolutions.vendormanagement.service.attachment.AttachmentService;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@Path("/api/v1/attachments")
@Produces(MediaType.APPLICATION_JSON)
public class AttachmentResource {

  @Inject
  AttachmentService attachmentService;

  // #region GET METHODS

  /**
   * GET /api/v1/attachments?moduleType=VENDOR&referenceCode=1
   * Get all attachments for a module/reference
   */
  @GET
  public Uni<Response> getAttachments(
      @QueryParam("moduleType") String moduleType,
      @QueryParam("referenceCode") String referenceCode) {

    if (moduleType == null || moduleType.isBlank()) {
      return Uni.createFrom().item(
          Response.status(Response.Status.BAD_REQUEST)
              .entity(Map.of("error", "moduleType is required"))
              .build());
    }
    if (referenceCode == null || referenceCode.isBlank()) {
      return Uni.createFrom().item(
          Response.status(Response.Status.BAD_REQUEST)
              .entity(Map.of("error", "referenceCode is required"))
              .build());
    }

    return attachmentService.getAttachments(moduleType, referenceCode)
        .onItem().transform(attachments -> Response.ok(attachments).build())
        .onFailure().recoverWithItem(error -> {
          System.err.println("Error fetching attachments: " + error.getMessage());
          return Response.serverError()
              .entity(Map.of("error", error.getMessage()))
              .build();
        });
  }

  /**
   * GET /api/v1/attachments/download/{id}
   * Download attachment file
   */
  @GET
  @Path("/download/{id}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Uni<Response> downloadAttachment(@PathParam("id") Long id) {
    if (id == null) {
      return Uni.createFrom().item(
        Response.status(Response.Status.BAD_REQUEST)
          .entity("Attachment ID is required")
          .build()
      );
    }

    // Get metadata first, then download content
    return attachmentService.getAttachmentForDownload(id)
      .flatMap(attachment -> {
        if (attachment == null) {
          return Uni.createFrom().item(
            Response.status(Response.Status.NOT_FOUND)
              .entity("Attachment not found")
              .build()
          );
        }

        // Download file content (from FTP or database)
        return attachmentService.downloadFile(id)
          .onItem().transform(fileData -> Response.ok(fileData)
            .header("Content-Disposition",
                "attachment; filename=\"" + attachment.getOriginalName() + "\"")
            .header("Content-Type",
                attachment.getContentType() != null
                  ? attachment.getContentType()
                  : "application/octet-stream")
            .build()
          );
      })
      .onFailure().recoverWithItem(error -> {
        System.err.println("Error downloading file: " + error.getMessage());
        return Response.serverError()
          .entity("Failed to download file: " + error.getMessage())
          .build();
      });
  }

  // #endregion

  // #region POST METHODS

  /**
   * POST /api/v1/attachments/upload
   * Upload single file
   */
  @POST
  @Path("/upload")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Uni<Response> uploadFile(
      @RestForm("file") FileUpload file,
      @RestForm("moduleType") String moduleType,
      @RestForm("referenceCode") String referenceCode,
      @RestForm("description") String description) {

    // Validate file
    if (file == null) {
      return Uni.createFrom().item(
          Response.status(Response.Status.BAD_REQUEST)
              .entity(Map.of("error", "No file provided"))
              .build());
    }

    // Read file data
    byte[] fileData;
    try {
      fileData = Files.readAllBytes(file.uploadedFile());
    } catch (IOException e) {
      System.err.println("Error reading uploaded file: " + e.getMessage());
      return Uni.createFrom().item(
          Response.serverError()
              .entity(Map.of("error", "Failed to read uploaded file"))
              .build());
    }

    String originalName = file.fileName();
    String contentType = file.contentType();

    return attachmentService.uploadFile(
        moduleType,
        referenceCode,
        originalName,
        contentType,
        fileData)
        .onItem().transform(attachment -> Response.status(Response.Status.CREATED)
            .entity(attachment)
            .build())
        .onFailure(IllegalArgumentException.class).recoverWithItem(error -> Response.status(Response.Status.BAD_REQUEST)
            .entity(Map.of("error", error.getMessage()))
            .build())
        .onFailure().recoverWithItem(error -> {
          System.err.println("Error uploading file: " + error.getMessage());
          error.printStackTrace();
          return Response.serverError()
              .entity(Map.of("error", "Failed to upload file"))
              .build();
        });
  }

  /**
   * POST /api/v1/attachments/upload-multiple
   * Upload multiple files
   */
  @POST
  @Path("/upload-multiple")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Uni<Response> uploadMultipleFiles(
      @RestForm("files") List<FileUpload> files,
      @RestForm("moduleType") String moduleType,
      @RestForm("referenceCode") String referenceCode) {

    // Validate files
    if (files == null || files.isEmpty()) {
      return Uni.createFrom().item(
          Response.status(Response.Status.BAD_REQUEST)
              .entity(Map.of("error", "No files provided"))
              .build());
    }

    // Upload files sequentially
    Uni<Integer> uploadChain = Uni.createFrom().item(0);

    for (FileUpload file : files) {
      uploadChain = uploadChain.chain(count -> {
        byte[] fileData;
        try {
          fileData = Files.readAllBytes(file.uploadedFile());
        } catch (IOException e) {
          return Uni.createFrom().item(count); // Skip failed file
        }

        return attachmentService.uploadFile(
            moduleType,
            referenceCode,
            file.fileName(),
            file.contentType(),
            fileData)
            .onItem().transform(attachment -> count + 1)
            .onFailure().recoverWithItem(count); // Skip failed file
      });
    }

    return uploadChain
        .onItem().transform(successCount -> Response.status(Response.Status.CREATED)
            .entity(Map.of(
                "success", true,
                "message", successCount + " of " + files.size() + " files uploaded successfully",
                "uploadedCount", successCount,
                "totalCount", files.size()))
            .build())
        .onFailure().recoverWithItem(error -> {
          System.err.println("Error uploading files: " + error.getMessage());
          return Response.serverError()
              .entity(Map.of("error", "Failed to upload files"))
              .build();
        });
  }

  // #endregion

  // #region DELETE METHODS

  /**
   * DELETE /api/v1/attachments/{id}
   * Delete attachment
   */
  @DELETE
  @Path("/{id}")
  public Uni<Response> deleteAttachment(@PathParam("id") Long id) {
    if (id == null) {
      return Uni.createFrom().item(
          Response.status(Response.Status.BAD_REQUEST)
              .entity(Map.of("error", "Attachment ID is required"))
              .build());
    }
    return attachmentService.deleteAttachment(id)
        .onItem().transform(deleted -> {
          if (deleted) {
            return Response.ok(Map.of("success", true, "message", "Attachment deleted")).build();
          }
          return Response.status(Response.Status.NOT_FOUND)
              .entity(Map.of("error", "Attachment not found"))
              .build();
        })
        .onFailure().recoverWithItem(error -> {
          System.err.println("Error deleting attachment: " + error.getMessage());
          return Response.serverError()
              .entity(Map.of("error", error.getMessage()))
              .build();
        });
  }

  // #endregion
}