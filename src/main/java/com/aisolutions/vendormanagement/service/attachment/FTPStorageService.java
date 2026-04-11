package com.aisolutions.vendormanagement.service.attachment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Service for handling file storage via FTP
 * 
 * Files are stored in: {basePath}/{moduleType}/{referenceCode}/{uniqueFileName}
 * Example: /vendor-attachments/VENDOR/123/a1b2c3d4-report.pdf
 */
@ApplicationScoped
public class FTPStorageService {

  @ConfigProperty(name = "ftp.host")
  String host;

  @ConfigProperty(name = "ftp.port", defaultValue = "21")
  int port;

  @ConfigProperty(name = "ftp.username")
  String username;

  @ConfigProperty(name = "ftp.password")
  String password;

  @ConfigProperty(name = "ftp.base-path", defaultValue = "/vendor-attachments")
  String basePath;

  private static final int CONNECT_TIMEOUT_MS = 30000;
  private static final Duration DATA_TIMEOUT = Duration.ofSeconds(60);

  // #region PUBLIC METHODS

  public Uni<String> uploadFile(byte[] fileData, String moduleType, String referenceCode, String originalName) {
    return Uni.createFrom().item(() -> {
      FTPClient ftpClient = new FTPClient();

      try {
        // Connect to FTP server
        connect(ftpClient);

        // Build directory path: /vendor-attachments/VENDOR/123
        String directoryPath = buildDirectoryPath(moduleType, referenceCode);

        // Create directories if not exist
        createDirectories(ftpClient, directoryPath);

        // Generate unique filename
        String uniqueFileName = generateUniqueFileName(originalName);

        // Full remote path
        String remotePath = directoryPath + "/" + uniqueFileName;

        // Upload file
        try (InputStream inputStream = new ByteArrayInputStream(fileData)) {
          boolean success = ftpClient.storeFile(remotePath, inputStream);
          if (!success) {
            throw new RuntimeException("Failed to upload file. FTP reply: " + ftpClient.getReplyString());
          }
        }

        System.out.println("File uploaded successfully to: " + remotePath);
        return remotePath;

      } catch (Exception e) {
        System.err.println("FTP upload error: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Failed to upload file to FTP server: " + e.getMessage(), e);
      } finally {
        disconnect(ftpClient);
      }
    });
  }

  public Uni<byte[]> downloadFile(String remotePath) {
    return Uni.createFrom().item(() -> {
      FTPClient ftpClient = new FTPClient();

      try {
        connect(ftpClient);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
          boolean success = ftpClient.retrieveFile(remotePath, outputStream);
          if (!success) {
            throw new RuntimeException("Failed to download file. FTP reply: " + ftpClient.getReplyString());
          }
          return outputStream.toByteArray();
        }

      } catch (Exception e) {
        System.err.println("FTP download error: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Failed to download file from FTP server: " + e.getMessage(), e);
      } finally {
        disconnect(ftpClient);
      }
    });
  }

  public Uni<Boolean> deleteFile(String remotePath) {
    return Uni.createFrom().item(() -> {
      FTPClient ftpClient = new FTPClient();

      try {
        connect(ftpClient);

        boolean deleted = ftpClient.deleteFile(remotePath);

        if (deleted) {
          System.out.println("File deleted successfully: " + remotePath);
        } else {
          // File might not exist, which is okay
          System.out.println("File not found or already deleted: " + remotePath);
        }

        return true; // Consider success even if file didn't exist

      } catch (Exception e) {
        System.err.println("FTP delete error: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Failed to delete file from FTP server: " + e.getMessage(), e);
      } finally {
        disconnect(ftpClient);
      }
    });
  }

  /**
   * Check if file exists on FTP server
   */
  public Uni<Boolean> fileExists(String remotePath) {
    return Uni.createFrom().item(() -> {
      FTPClient ftpClient = new FTPClient();

      try {
        connect(ftpClient);

        // Try to get file size. If it fails, file doesn't exist
        try (InputStream is = ftpClient.retrieveFileStream(remotePath)) {
          boolean exists = is != null && ftpClient.getReplyCode() != 550;
          ftpClient.completePendingCommand();
          return exists;
        }

      } catch (Exception e) {
        return false;
      } finally {
        disconnect(ftpClient);
      }
    });
  }

  // #endregion

  // #region PRIVATE HELPERS

  /**
   * Connect to FTP server
   */
  private void connect(FTPClient ftpClient) throws IOException {
    ftpClient.setConnectTimeout(CONNECT_TIMEOUT_MS);
    ftpClient.setDataTimeout(DATA_TIMEOUT);
    ftpClient.setDefaultTimeout(CONNECT_TIMEOUT_MS);

    // Connect
    ftpClient.connect(host, port);

    int replyCode = ftpClient.getReplyCode();
    if (!FTPReply.isPositiveCompletion(replyCode)) {
      ftpClient.disconnect();
      throw new RuntimeException("FTP server refused connection. Reply code: " + replyCode);
    }

    // Login
    boolean loggedIn = ftpClient.login(username, password);
    if (!loggedIn) {
      ftpClient.disconnect();
      throw new RuntimeException("Failed to login to FTP server. Check username/password.");
    }

    // Set binary mode for file transfer
    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

    // Use passive mode
    ftpClient.enterLocalPassiveMode();

    System.out.println("Connected to FTP server: " + host);
  }

  /**
   * Build directory path based on module type and reference code
   */
  private String buildDirectoryPath(String moduleType, String referenceCode) {
    return basePath + "/" + moduleType.toUpperCase() + "/" + referenceCode;
  }

  /**
   * Generate unique filename to prevent overwriting
   */
  private String generateUniqueFileName(String originalName) {
    String uuid = UUID.randomUUID().toString().substring(0, 8);

    // Extract extension
    int lastDot = originalName.lastIndexOf('.');
    if (lastDot > 0) {
      String name = originalName.substring(0, lastDot);
      String ext = originalName.substring(lastDot);
      // Sanitize filename (remove special characters)
      name = sanitizeFileName(name);
      return uuid + "-" + name + ext;
    }

    return uuid + "-" + sanitizeFileName(originalName);
  }

  /**
   * Sanitize filename - remove special characters
   */
  private String sanitizeFileName(String fileName) {
    return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
  }

  /**
   * Create directories recursively on FTP server
   */
  private void createDirectories(FTPClient ftpClient, String directoryPath) throws IOException {
    String[] folders = directoryPath.split("/");
    StringBuilder currentPath = new StringBuilder();

    for (String folder : folders) {
      if (folder.isEmpty())
        continue;

      currentPath.append("/").append(folder);
      String path = currentPath.toString();

      // Try to change to directory, if fails, create it
      boolean exists = ftpClient.changeWorkingDirectory(path);
      if (!exists) {
        boolean created = ftpClient.makeDirectory(path);
        if (created) {
          System.out.println("Created directory: " + path);
        }
        // Change to the new directory
        ftpClient.changeWorkingDirectory(path);
      }
    }

    // Go back to root
    ftpClient.changeWorkingDirectory("/");
  }

  /**
   * Safely disconnect from FTP server
   */
  private void disconnect(FTPClient ftpClient) {
    try {
      if (ftpClient.isConnected()) {
        ftpClient.logout();
        ftpClient.disconnect();
      }
    } catch (IOException e) {
      System.err.println("Error disconnecting from FTP: " + e.getMessage());
    }
  }

  // #endregion
}
