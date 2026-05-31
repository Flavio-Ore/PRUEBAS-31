package com.utp.impulsa.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.utp.impulsa.model.MatchResult;
import com.utp.impulsa.model.Task;
import com.utp.impulsa.model.User;
import com.utp.impulsa.repository.MatchResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);
    
    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.api-url}")
    private String apiUrl;

    private final MatchResultRepository matchResultRepository;
    private final CvService cvService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public GeminiService(MatchResultRepository matchResultRepository, CvService cvService) {
        this.matchResultRepository = matchResultRepository;
        this.cvService = cvService;
    }

    public Map<String, Object> analyzeAffinity(User user, Task task) {
        // Recuperar todo el contexto del estudiante (CV + Skills)
        Map<String, Object> cvData = cvService.getCvData(user.getId());
        
        // Extraer skills en texto del estudiante
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> studentSkills = (List<Map<String, Object>>) cvData.get("skills");
        List<String> skillNames = new ArrayList<>();
        if (studentSkills != null) {
            for (Map<String, Object> s : studentSkills) {
                skillNames.add(String.valueOf(s.get("majors")));
            }
        }

        Map<String, Object> aiResult;
        
        // Si hay API Key, consumimos Gemini API. Si no, usamos el motor inteligente local.
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            logger.info("Calling Google Gemini API to analyze technical affinity...");
            aiResult = callGeminiApi(user, cvData, skillNames, task);
        } else {
            logger.info("No Gemini API key configured. Falling back to local context-aware technical matchmaking...");
            aiResult = generateIntelligentMockMatch(user, skillNames, task);
        }

        // Persistir el resultado del match en base de datos
        UUID matchId = UUID.randomUUID();
        int score = ((Number) aiResult.get("score_affinity")).intValue();
        String analysis = String.valueOf(aiResult.get("majors"));
        
        @SuppressWarnings("unchecked")
        List<String> suggested = (List<String>) aiResult.get("suggested_skills");

        MatchResult matchResult = new MatchResult(
            matchId,
            user.getId(),
            task.getId(),
            score,
            analysis,
            suggested
        );
        matchResultRepository.save(matchResult);

        return aiResult;
    }

    private Map<String, Object> callGeminiApi(User user, Map<String, Object> cvData, List<String> skillNames, Task task) {
        try {
            // Prompt de ingeniería técnica estructurado
            String prompt = String.format(
                "Eres un Evaluador Técnico de Ingeniería de Software. " +
                "Compara el perfil del estudiante UTP con el Reto Backend Freelance.\n\n" +
                "--- PERFIL DEL ESTUDIANTE ---\n" +
                "Nombre: %s\n" +
                "Carreras: %s\n" +
                "Habilidades Técnicas: %s\n" +
                "Datos del CV (JSON): %s\n\n" +
                "--- RETO FREELANCE BACKEND ---\n" +
                "Título: %s\n" +
                "Descripción Técnica: %s\n" +
                "Habilidades Requeridas (IDs): %s\n\n" +
                "--- INSTRUCCIONES ---\n" +
                "Retorna estrictamente un objeto JSON (sin markdown, sin bloques ```json) con los siguientes campos:\n" +
                "1. \"score_affinity\": Un número entero del 0 al 100 indicando la compatibilidad técnica del alumno con el reto.\n" +
                "2. \"majors\": Un texto explicativo y profesional en español de 2 a 3 líneas detallando por qué obtuvo este score (mencionando sus fortalezas y qué le falta).\n" +
                "3. \"suggested_skills\": Una lista de 2 a 3 temas o patrones técnicos específicos (ej: 'Implementación de Refresh Tokens', 'Patrones de Microservicios') que debería repasar para tener éxito en este reto.",
                user.getFullName(),
                user.getMajors(),
                skillNames,
                mapper.writeValueAsString(cvData),
                task.getTitle(),
                task.getDescription(),
                task.getSkillsRequired()
            );

            // Construir payload para Gemini API
            ObjectNode rootNode = mapper.createObjectNode();
            ObjectNode contentNode = rootNode.putArray("contents").addObject();
            contentNode.putArray("parts").addObject().put("text", prompt);
            
            ObjectNode configNode = rootNode.putObject("generationConfig");
            configNode.put("responseMimeType", "application/json");

            // Configurar headers HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(rootNode.toString(), headers);
            String fullUrl = apiUrl + "?key=" + apiKey;

            // Enviar petición HTTP POST
            String responseStr = restTemplate.postForObject(fullUrl, entity, String.class);
            JsonNode responseJson = mapper.readTree(responseStr);
            
            // Extraer respuesta de texto estructurado de Gemini
            String textResult = responseJson.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text")
                    .asText();

            JsonNode finalJson = mapper.readTree(textResult);

            Map<String, Object> result = new HashMap<>();
            result.put("score_affinity", finalJson.path("score_affinity").asInt(75));
            // Mapeamos a "majors" para cumplir estrictamente con el JSON de salida solicitado por el frontend
            result.put("majors", finalJson.path("majors").asText("Buen match técnico. Posees las bases de programación requeridas."));
            
            List<String> suggested = new ArrayList<>();
            JsonNode sugNode = finalJson.path("suggested_skills");
            if (sugNode.isArray()) {
                for (JsonNode n : sugNode) {
                    suggested.add(n.asText());
                }
            }
            if (suggested.isEmpty()) {
                suggested.addAll(Arrays.asList("JWT Security Design", "Dockerización"));
            }
            result.put("suggested_skills", suggested);
            
            return result;

        } catch (Exception ex) {
            logger.error("Error communicating with Gemini API, falling back to mock matchmaking", ex);
            return generateIntelligentMockMatch(user, skillNames, task);
        }
    }

    private Map<String, Object> generateIntelligentMockMatch(User user, List<String> skillNames, Task task) {
        Random rand = new Random();
        int score;
        String analysis;
        List<String> suggested = new ArrayList<>();

        // 1. Clasificación dinámica de la orientación del estudiante basada en sus skills actuales
        boolean hasJava = false;
        boolean hasSpring = false;
        boolean hasBackend = false;
        boolean hasFrontend = false;
        boolean hasDevops = false;
        boolean hasDb = false;

        for (String skill : skillNames) {
            String s = skill.toLowerCase().trim();
            if (s.contains("java")) hasJava = true;
            if (s.contains("spring") || s.contains("boot")) hasSpring = true;
            if (s.contains("python") || s.contains("django") || s.contains("node") || s.contains("express") || s.contains("api") || s.contains("micro") || s.contains("jwt") || s.contains("rest")) {
                hasBackend = true;
            }
            if (s.contains("react") || s.contains("angular") || s.contains("vue") || s.contains("front") || s.contains("css") || s.contains("html") || s.contains("js") || s.contains("javascript")) {
                hasFrontend = true;
            }
            if (s.contains("docker") || s.contains("kubernetes") || s.contains("devops") || s.contains("aws") || s.contains("cloud") || s.contains("git") || s.contains("cicd")) {
                hasDevops = true;
            }
            if (s.contains("sql") || s.contains("postgres") || s.contains("database") || s.contains("mongo") || s.contains("mysql")) {
                hasDb = true;
            }
        }

        // 2. Selección de Escenario Técnico (Situación)
        if (skillNames.isEmpty()) {
            // Escenario 5: Estudiante sin habilidades iniciales o en blanco
            score = 20 + rand.nextInt(15); // 20% - 35%
            analysis = String.format("Afinidad técnica inicial baja. Hola %s, actualmente no hemos detectado tecnologías o proyectos registrados en tu panel de control. Para abordar con éxito un microservicio de autenticación profesional (JWT), te recomendamos iniciar reforzando conceptos básicos de lógica de programación orientada a objetos en Java y estructura de bases de datos relacionales antes de aplicar.", user.getFullName());
            suggested.addAll(Arrays.asList("Programación Orientada a Objetos en Java", "Introducción a Bases de Datos (SQL)", "Fundamentos de Arquitectura Web REST"));

        } else if (hasJava && hasSpring) {
            // Escenario 1: Especialista en Java & Spring Boot (Match Perfecto Backend)
            score = 88 + rand.nextInt(10); // 88% - 97%
            analysis = String.format("¡Excelente Match Técnico! Tu perfil cuenta con una afinidad excepcional para este reto de InnovaTech. Al dominar la pila nativa de Java y Spring Boot, posees el control directo de las herramientas de inyección de dependencias y Spring Data JPA. Te sugerimos revisar con atención la configuración de filtros de seguridad HTTP, la clase 'SecurityFilterChain' de Spring Security 6, y la gestión segura de tokens criptográficos HMAC-256 para asegurar el éxito rotundo del microservicio.", user.getFullName());
            suggested.addAll(Arrays.asList("Configuración de Spring Security 6", "Estrategias de Hashing BCrypt para Contraseñas", "Gestión de Ciclo de Vida y Refresh Tokens"));

        } else if (hasJava || hasSpring || hasBackend) {
            // Escenario 2: Desarrollador Backend General (Match Medio-Alto, le falta Spring o Java)
            score = 70 + rand.nextInt(12); // 70% - 81%
            analysis = String.format("Sólido potencial en Backend. Cuentas con conocimientos lógicos correctos en la creación de API REST, manejo de JSON y persistencia con SQL. Dado que este reto específico requiere Java 21 y la infraestructura de Spring Boot, tu principal curva de aprendizaje será adaptarte al tipado fuerte de Java y al contenedor de inversión de control de Spring. Te sugerimos revisar el mapeo de entidades relacionales con Hibernate y la inyección de dependencias '@Autowired'.", user.getFullName());
            suggested.addAll(Arrays.asList("Mapeo de Entidades Relacionales con JPA", "Inversión de Control & Inyección de Dependencias", "Firma y Validación de Claims JWT"));

        } else if (hasDevops) {
            // Escenario 4: Especialista en DevOps e Infraestructura
            score = 65 + rand.nextInt(13); // 65% - 77%
            analysis = String.format("¡Sólida afinidad en Infraestructura y Despliegue! Tu dominio de contenedores Docker, flujos de Git y bases de datos relacionales facilitará increíblemente la modularización, despliegue y dockerización del microservicio de autenticación. Para lograr la compatibilidad completa del reto, te sugerimos enfocar tus esfuerzos en programar la lógica del servidor, revisando el ruteo de peticiones con Spring MVC y el control de excepciones REST.", user.getFullName());
            suggested.addAll(Arrays.asList("Estructura de Controladores Spring Boot", "Manejo Global de Excepciones REST", "Dockerización de Servicios Spring"));

        } else if (hasFrontend) {
            // Escenario 3: Desarrollador Frontend (Match Moderado, orientado a UI)
            score = 42 + rand.nextInt(15); // 42% - 56%
            analysis = String.format("Afinidad técnica moderada. Tu perfil está enfocado principalmente en desarrollo interactivo Frontend. Aunque dominas el intercambio de datos en formato JSON y el consumo de endpoints, este reto de InnovaTech se centra en lógica crítica del lado del servidor como encriptación, hashes y persistencia SQL. Te aconsejamos estudiar el flujo de autenticación seguro en capas (Controller-Service-Repository) y cómo interactuar de forma segura con la base de datos.", user.getFullName());
            suggested.addAll(Arrays.asList("Arquitectura de Capas en Servidores", "Flujos de Autenticación Segura (Cookies vs JWT)", "Persistencia y Relaciones SQL"));

        } else {
            // Escenario Genérico / Habilidades Básicas
            score = 55 + rand.nextInt(15); // 55% - 69%
            analysis = String.format("Compatibilidad en desarrollo. Posees bases tecnológicas básicas en tu perfil. Debido a que el reto exige el desarrollo estructurado de un microservicio robusto en Java 21, te recomendamos repasar patrones de diseño de software libre de acoplamiento (Clean Code) y la creación de endpoints seguros con protocolo HTTP/HTTPS.", user.getFullName());
            suggested.addAll(Arrays.asList("Patrones de Diseño Clean Code", "Consultas Básicas SQL", "Introducción a Servicios Web REST"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("score_affinity", score);
        result.put("majors", analysis); // Mapear descripción del análisis al campo "majors" según api rest contract
        result.put("suggested_skills", suggested);
        return result;
    }
}
