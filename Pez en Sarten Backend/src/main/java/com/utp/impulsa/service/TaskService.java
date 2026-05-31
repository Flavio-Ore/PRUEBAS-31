package com.utp.impulsa.service;

import com.utp.impulsa.model.Task;
import com.utp.impulsa.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    // UUID del reto demo definido en seed.sql
    private static final UUID DEMO_TASK_ID = UUID.fromString("3a8f4c28-98d6-44c1-90a8-b649d21abff2");

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public Task getDemoTask() {
        return taskRepository.findById(DEMO_TASK_ID)
                .orElseGet(() -> {
                    // Retornar la primera tarea disponible si el UUID no coincide por alguna razón
                    return taskRepository.findAll().stream().findFirst()
                            .orElseThrow(() -> new NoSuchElementException("No hay tareas demo configuradas en la base de datos"));
                });
    }

    @Transactional(readOnly = true)
    public Task getTaskById(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException("Tarea no encontrada con ID: " + taskId));
    }
}
