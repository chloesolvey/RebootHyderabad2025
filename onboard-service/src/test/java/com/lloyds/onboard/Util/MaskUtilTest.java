package com.lloyds.onboard.Util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MaskUtilTest {

    // ---- maskEmail tests ----

    @Test
    void shouldMaskEmailCorrectly() {
        String input = "john.doe@example.com";
        String expected = "*******e@example.com";
        assertEquals(expected, MaskUtil.maskEmail(input));
    }

    @Test
    void shouldReturnEmailUnchangedIfNull() {
        assertNull(MaskUtil.maskEmail(null));
    }

    @Test
    void shouldReturnEmailUnchangedIfEmpty() {
        assertEquals("", MaskUtil.maskEmail(""));
    }

    @Test
    void shouldNotMaskEmailIfAtIsFirstChar() {
        assertEquals("@domain.com", MaskUtil.maskEmail("@domain.com"));
    }

    @Test
    void shouldNotMaskEmailIfAtIsSecondChar() {
        assertEquals("a@domain.com", MaskUtil.maskEmail("a@domain.com"));
    }

    // ---- maskMobileNumber tests ----

    @Test
    void shouldMaskMobileNumberCorrectly() {
        String input = "9876543210";
        String expected = "******3210";
        assertEquals(expected, MaskUtil.maskMobileNumber(input));
    }

    @Test
    void shouldReturnMobileUnchangedIfNull() {
        assertNull(MaskUtil.maskMobileNumber(null));
    }

    @Test
    void shouldReturnMobileUnchangedIfLessThanFourDigits() {
        assertEquals("123", MaskUtil.maskMobileNumber("123"));
    }

    @Test
    void shouldReturnMobileMaskedIfExactlyFourDigits() {
        assertEquals("1234", MaskUtil.maskMobileNumber("1234")); // No masking
    }

    @Test
    void shouldMaskMobileWithFiveDigits() {
        assertEquals("*1234", MaskUtil.maskMobileNumber("51234"));
    }
}