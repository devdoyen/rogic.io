package com.devdoyen.nemologic.controller;

import com.devdoyen.nemologic.security.AdminSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@SuppressWarnings("null")
public class AdminAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminSessionManager sessionManager;

    @BeforeEach
    public void setUp() {
        sessionManager.clear();
    }

    @Test
    public void testLoginSuccess() throws Exception {
        Map<String, String> creds = Map.of("username", "admin", "password", "admin123!");
        String json = objectMapper.writeValueAsString(creds);

        mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    public void testLoginFailure() throws Exception {
        Map<String, String> creds = Map.of("username", "admin", "password", "wrongpassword");
        String json = objectMapper.writeValueAsString(creds);

        mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testLogout() throws Exception {
        // Log in first to get a token
        Map<String, String> creds = Map.of("username", "admin", "password", "admin123!");
        String json = objectMapper.writeValueAsString(creds);

        String response = mockMvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andReturn().getResponse().getContentAsString();

        Map<?, ?> map = objectMapper.readValue(response, Map.class);
        String token = (String) map.get("token");

        // Request logout
        mockMvc.perform(post("/api/admin/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Verify token is removed from manager
        assertNull(sessionManager.getUsernameForToken(token));
    }

    @Test
    public void testUnauthorizedAccessToAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/stages"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testInvalidTokenAccessToAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/stages")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isForbidden());
    }
}
