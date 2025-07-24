package com.lloyds.onboard.controller;

import com.lloyds.onboard.config.ErrorConfig;
import com.lloyds.onboard.entity.RmUser;
import com.lloyds.onboard.exception.ServiceException;
import com.lloyds.onboard.repository.RmUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoginController.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RmUserRepository rmUserRepository;

    @MockBean
    private ErrorConfig errorConfig;

    @Test
    void shouldLoginSuccessfully() throws Exception {
        RmUser user = new RmUser();
        user.setName("John");
        user.setRole(RmUser.Role.ADMIN); // or whatever enum you're using

        when(rmUserRepository.findByRmidAndPassword("rm001", "pass123"))
                .thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "username": "rm001",
                      "password": "pass123"
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void shouldReturnErrorForInvalidCredentials() throws Exception {
        when(rmUserRepository.findByRmidAndPassword("rm001", "wrongpass"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "username": "rm001",
                      "password": "wrongpass"
                    }
                """))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertTrue(
                        result.getResolvedException() instanceof ServiceException
                ));
    }
}
