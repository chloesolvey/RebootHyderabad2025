package com.lloyds.onboard.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LoginRequestTest {

    @Test
    void shouldSetAndGetUsername() {
        LoginRequest login = new LoginRequest();
        login.setUsername("admin");

        assertEquals("admin", login.getUsername());
    }

    @Test
    void shouldSetAndGetPassword() {
        LoginRequest login = new LoginRequest();
        login.setPassword("secure123");

        assertEquals("secure123", login.getPassword());
    }

    @Test
    void shouldHandleNullValues() {
        LoginRequest login = new LoginRequest();
        login.setUsername(null);
        login.setPassword(null);

        assertNull(login.getUsername());
        assertNull(login.getPassword());
    }

    @Test
    void shouldSupportEmptyStrings() {
        LoginRequest login = new LoginRequest();
        login.setUsername("");
        login.setPassword("");

        assertEquals("", login.getUsername());
        assertEquals("", login.getPassword());
    }
}