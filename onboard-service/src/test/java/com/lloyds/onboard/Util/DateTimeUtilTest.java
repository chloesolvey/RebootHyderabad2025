package com.lloyds.onboard.Util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilTest {

    @Test
    void shouldFormatDateTimeCorrectly() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 6, 30, 14, 45, 30);
        String formatted = DateTimeUtil.format(dateTime);
        assertEquals("2024-06-30 14:45:30", formatted);
    }

    @Test
    void shouldReturnNullWhenDateTimeIsNull() {
        String formatted = DateTimeUtil.format(null);
        assertNull(formatted);
    }

}