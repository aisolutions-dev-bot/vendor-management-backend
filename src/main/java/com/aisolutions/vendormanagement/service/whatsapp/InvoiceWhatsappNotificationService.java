package com.aisolutions.vendormanagement.service.whatsapp;

import com.aisolutions.shared.service.whatsapp.MetaWhatsappService;
import com.aisolutions.shared.service.whatsapp.TemplateComponent;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * WhatsApp notifications for the vendor invoice workflow, mirroring
 * vendor-registration-backend's WhatsappNotificationService pattern.
 */
@ApplicationScoped
public class InvoiceWhatsappNotificationService {

    private static final Logger LOG = Logger.getLogger(InvoiceWhatsappNotificationService.class);
    private static final String LANGUAGE_CODE = "en_US";

    @Inject
    MetaWhatsappService metaWhatsappService;

    @Inject
    Vertx vertx;

    @ConfigProperty(name = "whatsapp.template.vendor-invoice-approval")
    String approvalTemplateName;

    @ConfigProperty(name = "whatsapp.template.vendor-invoice-review")
    String reviewTemplateName;

    public Uni<Boolean> sendInvoiceApprovalNotification(
            String recipientMobileNumber,
            String staffName,
            String vendorName,
            String invoiceNumber,
            String approveToken,
            String rejectToken) {

        String normalizedMobile = normalizeMobileNumber(recipientMobileNumber);

        LOG.infof("[WhatsApp] Sending %s — to=%s, staffName=%s, invoiceNumber=%s",
                approvalTemplateName, normalizedMobile, staffName, invoiceNumber);

        return vertx.executeBlocking(
            Uni.createFrom().item(() -> {
                metaWhatsappService.sendTemplate(
                    normalizedMobile,
                    approvalTemplateName,
                    LANGUAGE_CODE,
                    List.of(
                        TemplateComponent.bodyNamed(
                            new TemplateComponent.NamedParameter("staff_name", staffName),
                            new TemplateComponent.NamedParameter("vendor_name", vendorName),
                            new TemplateComponent.NamedParameter("invoice_number", invoiceNumber)
                        ),
                        TemplateComponent.buttonUrl(0, approveToken),
                        TemplateComponent.buttonUrl(1, rejectToken)
                    )
                );
                return true;
            })
        )
        .onFailure().invoke(err -> LOG.errorf(err, "[WhatsApp] sendTemplate FAILED — to=%s, cause=%s",
                normalizedMobile, err.getMessage()))
        .onFailure().recoverWithItem(false);
    }

    public Uni<Boolean> sendInvoiceReviewNotification(
            String recipientMobileNumber,
            String staffName,
            String vendorName,
            String invoiceNumber,
            String reviewToken,
            String rejectToken) {

        String normalizedMobile = normalizeMobileNumber(recipientMobileNumber);

        LOG.infof("[WhatsApp] Sending %s — to=%s, staffName=%s, invoiceNumber=%s",
                reviewTemplateName, normalizedMobile, staffName, invoiceNumber);

        return vertx.executeBlocking(
            Uni.createFrom().item(() -> {
                metaWhatsappService.sendTemplate(
                    normalizedMobile,
                    reviewTemplateName,
                    LANGUAGE_CODE,
                    List.of(
                        TemplateComponent.bodyNamed(
                            new TemplateComponent.NamedParameter("staff_name", staffName),
                            new TemplateComponent.NamedParameter("vendor_name", vendorName),
                            new TemplateComponent.NamedParameter("invoice_number", invoiceNumber)
                        ),
                        TemplateComponent.buttonUrl(0, reviewToken),
                        TemplateComponent.buttonUrl(1, rejectToken)
                    )
                );
                return true;
            })
        )
        .onFailure().invoke(err -> LOG.errorf(err, "[WhatsApp] sendTemplate FAILED — to=%s, cause=%s",
                normalizedMobile, err.getMessage()))
        .onFailure().recoverWithItem(false);
    }

    private String normalizeMobileNumber(String mobileNumber) {
        if (mobileNumber == null) return null;
        return mobileNumber.startsWith("+") ? mobileNumber.substring(1) : mobileNumber;
    }
}
