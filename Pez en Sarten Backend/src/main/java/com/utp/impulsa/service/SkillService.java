package com.utp.impulsa.service;

import com.utp.impulsa.model.Skill;
import com.utp.impulsa.model.User;
import com.utp.impulsa.repository.SkillRepository;
import com.utp.impulsa.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public SkillService(SkillRepository skillRepository, UserRepository userRepository) {
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
    }

    public List<Map<String, Object>> searchSkills(String query) {
        List<Skill> skills = skillRepository.findByNameContainingIgnoreCase(query);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Skill s : skills) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            // El contrato de API especifica "majors" como el campo de texto de la skill: { "id": 1, "majors": "Java" }
            map.put("majors", s.getName());
            result.add(map);
        }
        return result;
    }

    @Transactional
    public void saveOnboarding(UUID userId, List<String> majors, List<Integer> skillIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        
        // Actualizar carreras
        if (majors != null) {
            user.setMajors(majors);
            userRepository.save(user);
        }

        // Limpiar skills previas en la tabla intermedia
        entityManager.createNativeQuery("DELETE FROM user_skills WHERE user_id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();

        // Asociar las nuevas skills seleccionadas
        if (skillIds != null && !skillIds.isEmpty()) {
            for (Integer skillId : skillIds) {
                entityManager.createNativeQuery("INSERT INTO user_skills (user_id, skill_id) VALUES (:userId, :skillId) ON CONFLICT DO NOTHING")
                        .setParameter("userId", userId)
                        .setParameter("skillId", skillId)
                        .executeUpdate();
            }
        }
    }
}
