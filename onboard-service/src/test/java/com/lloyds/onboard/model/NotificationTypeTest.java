package com.lloyds.onboard.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationTypeTest {

    @Test
    void shouldReturnCorrectServiceNameForSms() {
        assertEquals("smsService", NotificationType.SMS.getServiceName());
    }

    @Test
    void shouldReturnCorrectServiceNameForMail() {
        assertEquals("mailService", NotificationType.MAIL.getServiceName());
    }

    @Test
    void shouldReturnCorrectServiceNameForSmsAndMail() {
        assertEquals("sms_and_mailService", NotificationType.SMS_AND_MAIL.getServiceName());
    }

    @Test
    void shouldEnumContainAllExpectedValues() {
        NotificationType[] types = NotificationType.values();
        assertArrayEquals(
                new NotificationType[]{NotificationType.SMS, NotificationType.MAIL, NotificationType.SMS_AND_MAIL},
                types
        );
    }
}