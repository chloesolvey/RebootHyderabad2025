package com.lloyds.onboard.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LoginResponseTest {

    @Test
    void shouldCreateLoginResponseWithCorrectValues() {
        LoginResponse response = new LoginResponse("success", "swapna", "ADMIN");

        assertEquals("success", response.getStatus());
        assertEquals("swapna", response.getUsername());
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void shouldHandleNullValuesGracefully() {
        LoginResponse response = new LoginResponse(null, null, null);

        assertNull(response.getStatus());
        assertNull(response.getUsername());
        assertNull(response.getRole());
    }

    @Test
    void shouldSupportEmptyStrings() {
        LoginResponse response = new LoginResponse("", "", "");

        assertEquals("", response.getStatus());
        assertEquals("", response.getUsername());
        assertEquals("", response.getRole());
    }
}