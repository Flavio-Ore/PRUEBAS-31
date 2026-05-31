package com.utp.impulsa.controller;

import com.utp.impulsa.service.SkillService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping("/api/skills/search")
    public ResponseEntity<?> searchSkills(@RequestParam(value = "q", defaultValue = "") String query) {
        List<Map<String, Object>> skills = skillService.searchSkills(query);
        return ResponseEntity.ok(skills);
    }

    @PostMapping("/api/onboarding/survey")
    public ResponseEntity<?> saveOnboarding(@RequestBody Map<String, Object> request) {
        try {
            UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            // Extraer majors (carreras)
            @SuppressWarnings("unchecked")
            List<String> majors = (List<String>) request.get("majors");
            
            // Extraer IDs de habilidades con soporte robusto
            List<Integer> skillIds = new ArrayList<>();
            Object skillsData = request.get("skills");
            
            if (skillsData instanceof List) {
                for (Object item : (List<?>) skillsData) {
                    if (item instanceof Number) {
                        skillIds.add(((Number) item).intValue());
                    }
                }
            } else {
                // Búsqueda de respaldo si hay otras listas de enteros en el JSON
                for (Map.Entry<String, Object> entry : request.entrySet()) {
                    if (entry.getValue() instanceof List && !entry.getKey().equalsIgnoreCase("majors")) {
                        for (Object item : (List<?>) entry.getValue()) {
                            if (item instanceof Number) {
                                skillIds.add(((Number) item).intValue());
                            }
                        }
                    }
                }
            }
            
            skillService.saveOnboarding(userId, majors, skillIds);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Onboarding completado");
            return ResponseEntity.ok(response);
            
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error procesando onboarding: " + ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
