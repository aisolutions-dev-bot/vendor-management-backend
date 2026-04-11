
package com.aisolutions.vendormanagement.service.email;

import com.aisolutions.shared.service.email.*;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Quarkus wrapper for the shared EmailService using injected config.
 */
@ApplicationScoped
public class EmailNotificationService {

  @ConfigProperty(name = "google.oauth.client-id")
  String clientId;

  @ConfigProperty(name = "google.oauth.client-secret")
  String clientSecret;

  @ConfigProperty(name = "google.oauth.refresh-token")
  String refreshToken;

  @ConfigProperty(name = "mail.sender-email")
  String senderEmail;

  private EmailService emailService;

  @PostConstruct
  void init() throws GeneralSecurityException, IOException {
    EmailConfig config = new EmailConfig();
    config.setClientId(clientId);
    config.setClientSecret(clientSecret);
    config.setRefreshToken(refreshToken);
    config.setSenderEmail(senderEmail);

    this.emailService = new EmailService(config);
  }

  public Uni<Boolean> sendReactive(String to, String subject, String body) {
    return Uni.createFrom().item(() -> {
      emailService.sendEmail(to, subject, body); // blocking call
      return true;
    }).onFailure().recoverWithItem(false);
  }

  public void sendEmail(String to, String subject, String body) {
    emailService.sendEmail(to, subject, body);
  }

  public void sendEmailWithAttachments(String to, String subject, String body, List<EmailAttachment> attachments) {
    emailService.sendEmailWithAttachments(to, subject, body, attachments);
  }
}
