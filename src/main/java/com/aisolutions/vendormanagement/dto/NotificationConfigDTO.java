package com.aisolutions.vendormanagement.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationConfigDTO {
  private boolean emailEnabled;
  private boolean smsEnabled;
  private boolean whatsappEnabled;
}
