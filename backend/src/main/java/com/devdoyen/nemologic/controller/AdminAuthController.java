package com.devdoyen.nemologic.controller;

import com.devdoyen.nemologic.security.AdminSessionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AdminSessionManager sessionManager;

    @Value("${admin.username:admin}")
    private String adminUsername;

    @Value("${admin.password:admin123!}")
    private String adminPassword;

    public AdminAuthController(AdminSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (adminUsername.equals(username) && adminPassword.equals(password)) {
            String token = UUID.randomUUID().toString();
            sessionManager.registerToken(token, username);
            return ResponseEntity.ok(Map.of("token", token));
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            sessionManager.removeToken(token);
        }
        return ResponseEntity.ok().build();
    }
}
