package com.lloyds.onboard.Util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilTest {

    private final String secretKey = "1234567890123456"; // 16-byte key for AES
    private final String sampleData = "Hello Copilot";

    @Test
    void shouldEncryptAndDecryptSuccessfully() throws Exception {
        String encrypted = EncryptionUtil.encrypt(sampleData, secretKey);
        assertNotNull(encrypted);
        assertNotEquals(sampleData, encrypted);

        String decrypted = EncryptionUtil.decrypt(encrypted, secretKey);
        assertEquals(sampleData, decrypted);
    }

    @Test
    void shouldThrowExceptionForInvalidKeyLength() {
        String shortKey = "short";

        assertThrows(Exception.class, () -> EncryptionUtil.encrypt(sampleData, shortKey));
        assertThrows(Exception.class, () -> EncryptionUtil.decrypt("invalid-data", shortKey));
    }

    @Test
    void shouldThrowExceptionOnInvalidEncryptedInput() {
        String invalidBase64 = "!!!not-base64";

        assertThrows(Exception.class, () -> EncryptionUtil.decrypt(invalidBase64, secretKey));
    }

    @Test
    void shouldEncryptToDifferentValueThanPlaintext() throws Exception {
        String encrypted = EncryptionUtil.encrypt(sampleData, secretKey);
        assertNotEquals(sampleData, encrypted);
    }
}