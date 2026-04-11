package com.aisolutions.vendormanagement.service.useractionlog;

import com.aisolutions.vendormanagement.repository.UserActionLogRepository;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UserActionLogService {

  @Inject
  UserActionLogRepository auditLogRepository;

  /**
   * Module constants
   */
  public static class Module {
    public static final String AP_RECON = "ACC-AP-RECON";
    // Add more modules as needed later
  }

  /**
   * Action constants
   */
  public static class Action {
    public static final String ADD = "ADD";
    public static final String EDIT = "EDIT";
    public static final String DELETE = "DELETE";
  }

  /**
   * Log an action with device info
   */
  public Uni<Void> logAction(
      String currentUser,
      String module,
      String referenceNo,
      String action,
      DeviceInfo deviceInfo,
      String remarks) {

    // If no user (auth failed), still log with null user
    String staffId = currentUser != null ? currentUser : null;

    return auditLogRepository.createLog(
        staffId,
        module,
        referenceNo,
        action,
        deviceInfo != null ? deviceInfo.getDeviceName() : null,
        deviceInfo != null ? deviceInfo.getDeviceIPAddress() : null,
        deviceInfo != null ? deviceInfo.getDeviceSerialNo() : null,
        remarks)
        .replaceWithVoid()
        .onFailure().recoverWithNull(); // Don't fail the main operation if logging fails
  }

  /**
   * Log an action without device info (for backward compatibility)
   */
  public Uni<Void> logAction(
      String currentUser,
      String module,
      String referenceNo,
      String action) {

    return logAction(currentUser, module, referenceNo, action, null, null);
  }

  /**
   * Device information holder
   */
  public static class DeviceInfo {
    private String deviceName;
    private String deviceIPAddress;
    private String deviceSerialNo;

    public DeviceInfo(String deviceName, String deviceIPAddress, String deviceSerialNo) {
      this.deviceName = deviceName;
      this.deviceIPAddress = deviceIPAddress;
      this.deviceSerialNo = deviceSerialNo;
    }

    public String getDeviceName() {
      return deviceName;
    }

    public String getDeviceIPAddress() {
      return deviceIPAddress;
    }

    public String getDeviceSerialNo() {
      return deviceSerialNo;
    }
  }
}
