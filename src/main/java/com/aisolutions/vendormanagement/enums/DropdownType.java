package com.aisolutions.vendormanagement.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * All available dropdown types
 */
@Getter
@RequiredArgsConstructor
public enum DropdownType {
  // Vendor
  VENDOR("vendor"), // All vendors
  ;

  private final String key;

  public static DropdownType fromKey(String key) {
    if (key == null)
      return null;
    for (DropdownType type : values()) {
      if (type.key.equalsIgnoreCase(key)) {
        return type;
      }
    }
    return null;
  }
}