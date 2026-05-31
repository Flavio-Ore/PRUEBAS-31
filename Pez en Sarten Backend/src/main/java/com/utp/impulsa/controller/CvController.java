package com.utp.impulsa.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.utp.impulsa.service.CvService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cv")
public class CvController {

    private final CvService cvService;

    public CvController(CvService cvService) {
        this.cvService = cvService;
    }

    @GetMapping("/data")
    public ResponseEntity<?> getCvData() {
        try {
            UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Map<String, Object> cvData = cvService.getCvData(userId);
            return ResponseEntity.ok(cvData);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error cargando CV: " + ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/data")
    public ResponseEntity<?> updateCvData(@RequestBody JsonNode request) {
        try {
            UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            String section = request.path("section").asText();
            
            // El contenido viene en la propiedad "majors" (typo del frontend) o "data"
            JsonNode data = request.path("majors");
            if (data.isMissingNode() || data.isNull()) {
                data = request.path("data");
            }
            
            cvService.updateCvData(userId, section, data);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "updated");
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error actualizando CV: " + ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/settings")
    public ResponseEntity<?> getCvSettings() {
        try {
            UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Map<String, Object> settings = cvService.getCvSettings(userId);
            return ResponseEntity.ok(settings);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error obteniendo configuraciones: " + ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/settings")
    public ResponseEntity<?> updateCvSettings(@RequestBody Map<String, Object> request) {
        try {
            UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            cvService.updateCvSettings(userId, request);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "settings_updated");
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error actualizando configuraciones: " + ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
