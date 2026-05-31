package com.utp.impulsa.controller;

import com.utp.impulsa.model.Task;
import com.utp.impulsa.model.User;
import com.utp.impulsa.service.AuthService;
import com.utp.impulsa.service.GeminiService;
import com.utp.impulsa.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class TaskController {

    private final TaskService taskService;
    private final AuthService authService;
    private final GeminiService geminiService;

    public TaskController(TaskService taskService, AuthService authService, GeminiService geminiService) {
        this.taskService = taskService;
        this.authService = authService;
        this.geminiService = geminiService;
    }

    @GetMapping("/api/tasks/prueba")
    public ResponseEntity<?> getDemoTask() {
        try {
            Task task = taskService.getDemoTask();
            
            // Cumplir con el contrato exacto del frontend:
            // { "task_id": "uuid", "majors": "...", "skills_required": [1, 5, 8] }
            Map<String, Object> response = new HashMap<>();
            response.put("task_id", task.getId().toString());
            response.put("majors", task.getDescription()); // descripción mapeada a "majors" según api rest contract
            response.put("title", task.getTitle());
            response.put("company_name", task.getCompanyName());
            response.put("company_logo", task.getCompanyLogo());
            response.put("description", task.getDescription());
            response.put("skills_required", task.getSkillsRequired());
            
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error obteniendo tarea de prueba: " + ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/api/ai/match")
    public ResponseEntity<?> analyzeMatch(@RequestBody Map<String, String> request) {
        try {
            UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User user = authService.getUserContext(userId);
            
            String taskIdStr = request.get("task_id");
            if (taskIdStr == null || taskIdStr.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El campo 'task_id' es requerido"));
            }
            
            UUID taskId = UUID.fromString(taskIdStr);
            Task task = taskService.getTaskById(taskId);
            
            Map<String, Object> matchResult = geminiService.analyzeAffinity(user, task);
            return ResponseEntity.ok(matchResult);
            
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID de tarea inválido o mal estructurado"));
        } catch (Exception ex) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error realizando análisis de afinidad IA: " + ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
