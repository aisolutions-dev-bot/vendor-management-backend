package com.aisolutions.vendormanagement.config;

import io.smallrye.config.ConfigMapping;
import jakarta.validation.constraints.NotBlank;

@ConfigMapping(prefix = "app.email")
public interface SmtpConfig {

  @NotBlank
  String host();

  int port();

  @NotBlank
  String username();

  @NotBlank
  String password();

  @NotBlank
  String senderEmail();
}
