package com.lloyds.onboard.service.notification;

import com.lloyds.onboard.Util.EncryptionUtil;
import com.lloyds.onboard.exception.ServiceException;
import com.lloyds.onboard.model.Constants;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mailService, "secretKey", "testKey123");
        ReflectionTestUtils.setField(mailService, "resumeUrl", "https://test.link/");
    }

    @Test
    void shouldSendMailNotificationSuccessfully() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email-template"), any(Context.class))).thenReturn("<html>Email Content</html>");

        try (MockedStatic<EncryptionUtil> encryptMock = mockStatic(EncryptionUtil.class)) {
            encryptMock.when(() -> EncryptionUtil.encrypt("APP001", "testKey123")).thenReturn("encryptedToken");

            mailService.sendNotification("APP001", "user@example.com", "John", "Personal Loan");

            verify(mailSender).send(mimeMessage);
        }
    }

    @Test
    void shouldThrowServiceExceptionOnEncryptionFailure() {
        try (MockedStatic<EncryptionUtil> encryptMock = mockStatic(EncryptionUtil.class)) {
            encryptMock.when(() -> EncryptionUtil.encrypt("APP002", "testKey123"))
                    .thenThrow(new RuntimeException("Encryption failed"));

            ServiceException ex = assertThrows(ServiceException.class, () ->
                    mailService.sendNotification("APP002", "user@example.com", "John", "Mortgage"));

            assertEquals(Constants.ENCRYPTION_ERROR, ex.getErrorCode());
        }
    }

    @Test
    void shouldThrowServiceExceptionOnEmailFailure() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email-template"), any(Context.class))).thenReturn("<html>Email Content</html>");
        doThrow(new RuntimeException("SMTP failed")).when(mailSender).send(mimeMessage);

        try (MockedStatic<EncryptionUtil> encryptMock = mockStatic(EncryptionUtil.class)) {
            encryptMock.when(() -> EncryptionUtil.encrypt("APP003", "testKey123")).thenReturn("encryptedToken");

            ServiceException ex = assertThrows(ServiceException.class, () ->
                    mailService.sendNotification("APP003", "user@example.com", "John", "Credit Card"));

            assertEquals(Constants.RESUME_JOURNEY_ERROR, ex.getErrorCode());
        }
    }
}