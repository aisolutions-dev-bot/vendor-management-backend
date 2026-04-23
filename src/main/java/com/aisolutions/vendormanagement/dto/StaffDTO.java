package com.aisolutions.vendormanagement.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal staff DTO for org-api staff lookup.
 * Only fields needed for email notification are mapped.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StaffDTO {

  private String staffId;
  private String name;
  private String emailCompany;
  private String emailPerson;

  /**
   * Returns company email if available, falls back to personal email.
   */
  public String getEffectiveEmail() {
    if (emailCompany != null && !emailCompany.isBlank()) return emailCompany;
    return emailPerson;
  }
}
