package com.lloyds.onboard.service.notification;

import com.lloyds.onboard.Util.EncryptionUtil;
import com.lloyds.onboard.exception.ServiceException;
import com.lloyds.onboard.model.Constants;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * Service implementation for sending email notifications.
 * <p>
 * Uses Thymeleaf templates to generate email content and sends emails asynchronously.
 * Encrypts applicationId to generate a secure resume journey link.
 * </p>
 */
@Service
@Slf4j
public class MailService implements NotificationService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${encryption.key}")
    private String secretKey;

    @Value("${resume-journey.url}")
    private String resumeUrl;

    /**
     * Constructs a MailService with the required mail sender and template engine.
     *
     * @param mailSender     the mail sender used to send emails
     * @param templateEngine the Thymeleaf template engine for processing email templates
     */
    public MailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Sends an email notification asynchronously with a resume journey link.
     * <p>
     * Encrypts the application ID, generates the email content from a template,
     * and sends the email to the specified recipient.
     * </p>
     *
     * @param applicationId the ID of the application to include in the email
     * @param email         the recipient's email address
     * @param name          the recipient's name (used in the email template)
     * @param journey       the journey name or description (used in the email template)
     * @throws ServiceException if encryption or sending the email fails
     */
    @Override
    @Async
    public void sendNotification(String applicationId, String email, String name, String journey) throws ServiceException {
        String token;
        try {
            token = EncryptionUtil.encrypt(applicationId, secretKey);
        } catch (Exception e) {
            log.error("Error encrypting applicationId", e);
            throw new ServiceException(Constants.ENCRYPTION_ERROR);
        }
        String resumeLink = resumeUrl + token;
        Map<String, Object> model = Map.of(
                "name", name,
                "journey", journey,
                "resumeLink", resumeLink
        );

        Context context = new Context();
        context.setVariables(model);

        String htmlContent = templateEngine.process("email-template", context);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject(Constants.EMAIL_SUBJECT + applicationId);
            helper.setText(htmlContent, true);
            log.info("Sending mail notification: " + resumeLink);

            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error sending mail notification", e);
            throw new ServiceException(Constants.RESUME_JOURNEY_ERROR);
        }
    }
}
