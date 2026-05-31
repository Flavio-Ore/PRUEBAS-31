package com.utp.impulsa.controller;

import com.utp.impulsa.model.User;
import com.utp.impulsa.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login/sso")
    public ResponseEntity<?> loginWithSso(@RequestBody Map<String, String> request) {
        String ssoToken = request.get("ssoToken");
        try {
            Map<String, Object> authData = authService.loginWithSso(ssoToken);
            return ResponseEntity.ok(authData);
        } catch (IllegalArgumentException ex) {
            Map<String, String> error = new HashMap<>();
            error.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/user/context")
    public ResponseEntity<?> getUserContext() {
        try {
            UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = authService.getUserContext(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("user_id", user.getId().toString());
            response.put("majors", user.getMajors());
            response.put("verified", user.isVerified());
            
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No autorizado o usuario no encontrado");
            return ResponseEntity.status(401).body(error);
        }
    }
}
