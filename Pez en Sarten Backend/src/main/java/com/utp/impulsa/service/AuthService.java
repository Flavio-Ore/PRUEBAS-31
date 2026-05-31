package com.utp.impulsa.service;

import com.utp.impulsa.config.JwtTokenProvider;
import com.utp.impulsa.model.CvData;
import com.utp.impulsa.model.CvSettings;
import com.utp.impulsa.model.User;
import com.utp.impulsa.repository.CvDataRepository;
import com.utp.impulsa.repository.CvSettingsRepository;
import com.utp.impulsa.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CvDataRepository cvDataRepository;
    private final CvSettingsRepository cvSettingsRepository;
    private final JwtTokenProvider tokenProvider;
    private final ObjectMapper mapper = new ObjectMapper();

    public AuthService(UserRepository userRepository,
                       CvDataRepository cvDataRepository,
                       CvSettingsRepository cvSettingsRepository,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.cvDataRepository = cvDataRepository;
        this.cvSettingsRepository = cvSettingsRepository;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public Map<String, Object> loginWithSso(String ssoToken) {
        // Validar token SSO / correo institucional
        String email = ssoToken;
        if (email == null || !email.contains("@")) {
            email = "jean.pool@utp.edu.pe";
        }
        
        if (!email.toLowerCase().endsWith("@utp.edu.pe")) {
            throw new IllegalArgumentException("Acceso exclusivo para correos institucionales @utp.edu.pe");
        }

        String fullName = extractNameFromEmail(email);
        
        // Buscar o crear usuario
        Optional<User> userOpt = userRepository.findByEmail(email);
        User user;
        boolean isNew = false;
        
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            user = new User(
                UUID.randomUUID(),
                email,
                fullName,
                Collections.singletonList("Ingeniería de Sistemas")
            );
            user = userRepository.save(user);
            isNew = true;
        }

        // Si es nuevo, provisionar CV y configuraciones estéticas vacías
        if (isNew) {
            initializeDefaultCvAndSettings(user.getId(), fullName, email);
        }

        // Generar JWT
        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getMajors());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId().toString());
        userMap.put("email", user.getEmail());
        userMap.put("fullName", user.getFullName());
        userMap.put("majors", user.getMajors());
        response.put("user", userMap);

        return response;
    }

    public User getUserContext(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }

    private void initializeDefaultCvAndSettings(UUID userId, String name, String email) {
        CvData cvData = new CvData(userId);
        
        // Crear JSON inicial para datos personales
        ObjectNode personalInfo = mapper.createObjectNode();
        personalInfo.put("name", name);
        personalInfo.put("email", email);
        personalInfo.put("phone", "+51 987 654 321");
        personalInfo.put("linkedin", "linkedin.com/in/" + name.toLowerCase().replace(" ", "-"));
        personalInfo.put("github", "github.com/" + name.toLowerCase().replace(" ", ""));
        personalInfo.put("summary", "Estudiante de Ingeniería apasionado por el desarrollo backend, bases de datos y desarrollo de sistemas robustos.");
        cvData.setPersonalInfo(personalInfo);
        
        // Listas vacías para el generador interactivo
        cvData.setEducation(mapper.createArrayNode());
        cvData.setExperience(mapper.createArrayNode());
        cvData.setProjects(mapper.createArrayNode());
        
        cvDataRepository.save(cvData);

        // Ajustes estéticos por defecto
        CvSettings settings = new CvSettings(userId);
        cvSettingsRepository.save(settings);
    }

    private String extractNameFromEmail(String email) {
        String namePart = email.split("@")[0];
        String[] parts = namePart.split("\\.");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)))
                  .append(part.substring(1))
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }
}
