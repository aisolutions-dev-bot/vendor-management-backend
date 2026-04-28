package com.aisolutions.vendormanagement.service.token;

import com.aisolutions.vendormanagement.entity.VendorInvActionToken;
import com.aisolutions.vendormanagement.repository.VendorInvActionTokenRepository;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@ApplicationScoped
public class InvoiceActionTokenService {

  private static final int TOKEN_EXPIRY_HOURS = 48;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

  @Inject
  VendorInvActionTokenRepository tokenRepository;

  // #region Generate

  /**
   * Generate a cryptographically secure 64-char hex token for APPROVE or REJECT.
   * Persists to DB with 48h expiry.
   */
  public Uni<String> generateToken(Long invoiceId, String invoiceNumber, String action, String approvalStaffId) {
    String tokenValue = generateSecureHex(64);
    LocalDateTime now = LocalDateTime.now();

    VendorInvActionToken token = new VendorInvActionToken();
    token.setToken(tokenValue);
    token.setInvoiceId(invoiceId);
    token.setInvoiceNumber(invoiceNumber);
    token.setAction(action);
    token.setApprovalStaffId(approvalStaffId);
    token.setExpiresAt(now.plusHours(TOKEN_EXPIRY_HOURS));
    token.setUsedAt(null);
    token.setCreatedAt(now);

    return Panache.withTransaction(() -> tokenRepository.insert(token))
        .onItem().transform(saved -> {
          log.info("Generated {} token for invoice {}: {}...{}", action, invoiceId,
              tokenValue.substring(0, 8), tokenValue.substring(56));
          return tokenValue;
        });
  }

  // #endregion

  // #region Validate & Consume

  public enum TokenResult {
    APPROVED, REJECTED, EXPIRED, ALREADY_USED, NOT_FOUND
  }

  public static class TokenValidationResult {
    public final TokenResult result;
    public final Long invoiceId;
    public final String invoiceNumber;
    public final String action;
    public final String approvalStaffId;

    public TokenValidationResult(TokenResult result, Long invoiceId, String invoiceNumber, String action, String approvalStaffId) {
      this.result = result;
      this.invoiceId = invoiceId;
      this.invoiceNumber = invoiceNumber;
      this.action = action;
      this.approvalStaffId = approvalStaffId;
    }

    public static TokenValidationResult of(TokenResult result) {
      return new TokenValidationResult(result, null, null, null, null);
    }
  }

  /**
   * Validate and atomically consume a token (one-time use).
   * Returns result indicating outcome.
   */
  public Uni<TokenValidationResult> validateAndConsume(String tokenValue) {
    return tokenRepository.findByToken(tokenValue)
        .onItem().transformToUni(token -> {
          if (token == null) {
            log.warn("Token not found: {}...{}", tokenValue.substring(0, 8), tokenValue.substring(56));
            return Uni.createFrom().item(TokenValidationResult.of(TokenResult.NOT_FOUND));
          }

          if (token.getUsedAt() != null) {
            log.warn("Token already used: invoiceId={} action={}", token.getInvoiceId(), token.getAction());
            return Uni.createFrom().item(TokenValidationResult.of(TokenResult.ALREADY_USED));
          }

          if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
            log.warn("Token expired: invoiceId={} expiredAt={}", token.getInvoiceId(), token.getExpiresAt());
            return Uni.createFrom().item(TokenValidationResult.of(TokenResult.EXPIRED));
          }

          // Atomically mark used — if another request beat us, markUsed returns 0
          return Panache.withTransaction(() ->
              tokenRepository.markUsed(tokenValue, LocalDateTime.now())
                  .onItem().transformToUni(updated -> {
                    if (updated == 0) {
                      log.warn("Token race condition — already consumed: invoiceId={}", token.getInvoiceId());
                      return Uni.createFrom().item(TokenValidationResult.of(TokenResult.ALREADY_USED));
                    }
                    TokenResult outcome = "APPROVE".equals(token.getAction())
                        ? TokenResult.APPROVED
                        : TokenResult.REJECTED;
                    log.info("Token consumed: invoiceId={} action={}", token.getInvoiceId(), token.getAction());
                    return Uni.createFrom().item(
                        new TokenValidationResult(outcome, token.getInvoiceId(), token.getInvoiceNumber(), token.getAction(), token.getApprovalStaffId()));
                  })
          );
        });
  }

  // #endregion

  // #region Helpers

  private static String generateSecureHex(int length) {
    byte[] bytes = new byte[length / 2];
    SECURE_RANDOM.nextBytes(bytes);
    StringBuilder sb = new StringBuilder(length);
    for (byte b : bytes) {
      sb.append(HEX_CHARS[(b >> 4) & 0xf]);
      sb.append(HEX_CHARS[b & 0xf]);
    }
    return sb.toString();
  }

  // #endregion
}
