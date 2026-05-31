package com.utp.impulsa.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utp.impulsa.model.CvData;
import com.utp.impulsa.model.CvSettings;
import com.utp.impulsa.repository.CvDataRepository;
import com.utp.impulsa.repository.CvSettingsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CvService {

    private final CvDataRepository cvDataRepository;
    private final CvSettingsRepository cvSettingsRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @PersistenceContext
    private EntityManager entityManager;

    public CvService(CvDataRepository cvDataRepository, CvSettingsRepository cvSettingsRepository) {
        this.cvDataRepository = cvDataRepository;
        this.cvSettingsRepository = cvSettingsRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCvData(UUID userId) {
        CvData cvData = cvDataRepository.findById(userId)
                .orElseGet(() -> {
                    CvData newCv = new CvData(userId);
                    newCv.setPersonalInfo(mapper.createObjectNode());
                    newCv.setEducation(mapper.createArrayNode());
                    newCv.setExperience(mapper.createArrayNode());
                    newCv.setProjects(mapper.createArrayNode());
                    return cvDataRepository.save(newCv);
                });

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("personal_info", cvData.getPersonalInfo());
        response.put("education", cvData.getEducation());
        response.put("experience", cvData.getExperience());
        response.put("projects", cvData.getProjects());

        // Recuperar y adjuntar las skills del estudiante en tiempo real
        List<Map<String, Object>> skillsList = getStudentSkills(userId);
        response.put("skills", skillsList);

        return response;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getStudentSkills(UUID userId) {
        List<Object[]> rows = entityManager.createNativeQuery(
                "SELECT s.id, s.name FROM skills s JOIN user_skills us ON s.id = us.skill_id WHERE us.user_id = :userId")
                .setParameter("userId", userId)
                .getResultList();

        List<Map<String, Object>> skills = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> skillMap = new HashMap<>();
            skillMap.put("id", row[0]);
            skillMap.put("majors", row[1]); // Mapear a "majors" para cumplir con la nomenclatura de tags del frontend
            skills.add(skillMap);
        }
        return skills;
    }

    @Transactional
    public void updateCvData(UUID userId, String section, JsonNode data) {
        CvData cvData = cvDataRepository.findById(userId)
                .orElseGet(() -> {
                    CvData newCv = new CvData(userId);
                    newCv.setPersonalInfo(mapper.createObjectNode());
                    newCv.setEducation(mapper.createArrayNode());
                    newCv.setExperience(mapper.createArrayNode());
                    newCv.setProjects(mapper.createArrayNode());
                    return cvDataRepository.save(newCv);
                });

        if ("personal".equalsIgnoreCase(section)) {
            cvData.setPersonalInfo(data);
        } else if ("education".equalsIgnoreCase(section)) {
            cvData.setEducation(data);
        } else if ("experience".equalsIgnoreCase(section)) {
            cvData.setExperience(data);
        } else if ("projects".equalsIgnoreCase(section)) {
            cvData.setProjects(data);
        } else {
            throw new IllegalArgumentException("Sección de CV no válida: " + section);
        }

        cvDataRepository.save(cvData);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCvSettings(UUID userId) {
        CvSettings settings = cvSettingsRepository.findById(userId)
                .orElseGet(() -> {
                    CvSettings defaults = new CvSettings(userId);
                    return cvSettingsRepository.save(defaults);
                });

        // Cumplir con el contrato JSON exacto del frontend:
        // { "themeColor": "#4F46E5", "majors": "font-sans", "majors": "space-y-5" } (duplicate keys in prompt)
        // Para soportar todas las deserializaciones frontend posibles sin romper nada, enviamos una estructura unificada
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("themeColor", settings.getThemeColor());
        response.put("fontFamily", settings.getFontFamily());
        response.put("spacing", settings.getSpacing());
        response.put("font", settings.getFontFamily()); // fallback
        response.put("space", settings.getSpacing()); // fallback
        return response;
    }

    @Transactional
    public void updateCvSettings(UUID userId, Map<String, Object> settingsMap) {
        CvSettings settings = cvSettingsRepository.findById(userId)
                .orElseGet(() -> new CvSettings(userId));

        if (settingsMap.containsKey("themeColor")) {
            settings.setThemeColor(String.valueOf(settingsMap.get("themeColor")));
        }

        // Leer font family y spacing soportando múltiples formas de envío por parte del frontend
        if (settingsMap.containsKey("fontFamily")) {
            settings.setFontFamily(String.valueOf(settingsMap.get("fontFamily")));
        } else if (settingsMap.containsKey("font")) {
            settings.setFontFamily(String.valueOf(settingsMap.get("font")));
        } else if (settingsMap.containsKey("majors")) {
            String val = String.valueOf(settingsMap.get("majors"));
            if (val.startsWith("font-")) {
                settings.setFontFamily(val);
            }
        }

        if (settingsMap.containsKey("spacing")) {
            settings.setSpacing(String.valueOf(settingsMap.get("spacing")));
        } else if (settingsMap.containsKey("space")) {
            settings.setSpacing(String.valueOf(settingsMap.get("space")));
        } else if (settingsMap.containsKey("majors")) {
            String val = String.valueOf(settingsMap.get("majors"));
            if (val.startsWith("space-")) {
                settings.setSpacing(val);
            }
        }

        cvSettingsRepository.save(settings);
    }
}
