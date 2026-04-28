package com.aisolutions.vendormanagement.service.email;

import com.aisolutions.shared.service.email.EmailConfig;
import com.aisolutions.shared.service.email.EmailService;
import com.aisolutions.shared.service.email.EmailAttachment;
import com.aisolutions.vendormanagement.config.SmtpConfig;

import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.Context;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.context.ManagedExecutor;

import java.util.List;

/**
 * Reactive SMTP email service using shared EmailService and injected SmtpConfig.
 */
@Slf4j
@ApplicationScoped
public class EmailNotificationService {

  private final EmailService emailService;

  @Inject
  ManagedExecutor managedExecutor;

  @Inject
  Vertx vertx;

  public EmailNotificationService(SmtpConfig smtpConfig) {
    EmailConfig config = new EmailConfig();
    config.setSenderEmail(smtpConfig.senderEmail());
    config.setSmtpPassword(smtpConfig.password());

    try {
      this.emailService = new EmailService(config, smtpConfig.host(), smtpConfig.port());
    } catch (Exception e) {
      throw new IllegalStateException("Failed to initialize EmailService with provided SMTP configuration", e);
    }
  }

  public Uni<Boolean> sendReactive(String to, String subject, String htmlBody) {
    Context context = vertx.getOrCreateContext();

    return Uni.createFrom().item(() -> {
      try {
        log.info("Sending email to: {} subject: {}", to, subject);
        emailService.sendEmail(to, subject, htmlBody);
        return true;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    })
        .runSubscriptionOn(managedExecutor)
        .emitOn(context::runOnContext)
        .onFailure().invoke(e -> log.error("Failed to send email to {}: {}", to, e.getMessage()))
        .onFailure().recoverWithItem(false);
  }

  public void sendEmailWithAttachments(String to, String subject, String body, List<EmailAttachment> attachments) {
    emailService.sendEmailWithAttachments(to, subject, body, attachments);
  }
}
