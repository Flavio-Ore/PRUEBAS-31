-- Datos de inicialización y simulación para Impulsa Job
-- Ejecutar en Supabase después de schema.sql

-- 1. Ampliar catálogo de Skills (IDs del 1 al 17)
INSERT INTO skills (id, name) VALUES
(1, 'Java'),
(2, 'Spring Boot'),
(3, 'Python'),
(4, 'Django'),
(5, 'SQL'),
(6, 'Microservicios'),
(7, 'REST API'),
(8, 'Docker'),
(9, 'Git'),
(10, 'JWT'),
(11, 'PostgreSQL'),
(12, 'React'),
(13, 'HTML'),
(14, 'CSS'),
(15, 'JavaScript'),
(16, 'AWS'),
(17, 'CI/CD')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name;

SELECT setval('skills_id_seq', (SELECT MAX(id) FROM skills));

-- 2. Insertar Tarea de Demostración Backend (Task Freelance)
INSERT INTO tasks (id, title, company_name, company_logo, description, skills_required) VALUES
(
  '3a8f4c28-98d6-44c1-90a8-b649d21abff2',
  'Microservicio de Autenticación con JWT',
  'InnovaTech Perú',
  'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=120&q=80',
  'Desarrollar un microservicio seguro y escalable en Java 21 y Spring Boot para gestionar el inicio de sesión, el registro de usuarios y la generación de tokens JWT. Deberá implementar mejores prácticas como almacenamiento de contraseñas con hashing (BCrypt), control de roles y despliegue modular en contenedores Docker.',
  ARRAY[1, 2, 5, 6, 7, 10, 11] -- Java, Spring Boot, SQL, Microservicios, REST API, JWT, PostgreSQL
)
ON CONFLICT (id) DO UPDATE SET 
  title = EXCLUDED.title,
  company_name = EXCLUDED.company_name,
  company_logo = EXCLUDED.company_logo,
  description = EXCLUDED.description,
  skills_required = EXCLUDED.skills_required;

-- 3. Pre-cargar Estudiantes Demo (Diferentes Orientaciones y Situaciones)

-- Estudiante A: jean.expert@utp.edu.pe (Especialista Java/Spring Boot)
INSERT INTO users (id, email, full_name, majors, verified) VALUES
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'jean.expert@utp.edu.pe', 'Jean Pool Experto', ARRAY['Ingeniería de Sistemas'], true)
ON CONFLICT (id) DO NOTHING;

-- Estudiante B: luis.backend@utp.edu.pe (Desarrollador Backend Python/SQL)
INSERT INTO users (id, email, full_name, majors, verified) VALUES
('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 'luis.backend@utp.edu.pe', 'Luis Backend', ARRAY['Ingeniería de Sistemas'], true)
ON CONFLICT (id) DO NOTHING;

-- Estudiante C: ana.frontend@utp.edu.pe (Desarrolladora Frontend React)
INSERT INTO users (id, email, full_name, majors, verified) VALUES
('c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f', 'ana.frontend@utp.edu.pe', 'Ana Front', ARRAY['Diseño y Desarrollo de Software'], true)
ON CONFLICT (id) DO NOTHING;

-- Estudiante D: carlos.devops@utp.edu.pe (Ingeniero DevOps e Infraestructura)
INSERT INTO users (id, email, full_name, majors, verified) VALUES
('d4e5f6a7-b8c9-0d1e-2f3a-4b5c6d7e8f9a', 'carlos.devops@utp.edu.pe', 'Carlos DevOps', ARRAY['Ingeniería de Redes y Comunicaciones'], true)
ON CONFLICT (id) DO NOTHING;


-- 4. Asociar Habilidades Técnicas a Estudiantes Demo

-- Jean Pool (Java, Spring Boot, SQL, Microservicios, REST, JWT, PostgreSQL)
INSERT INTO user_skills (user_id, skill_id) VALUES
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 1),
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 2),
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 5),
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 6),
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 7),
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 10),
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 11)
ON CONFLICT DO NOTHING;

-- Luis Backend (Python, Django, SQL, REST API, Git)
INSERT INTO user_skills (user_id, skill_id) VALUES
('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 3),
('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 4),
('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 5),
('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 7),
('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 9)
ON CONFLICT DO NOTHING;

-- Ana Front (React, HTML, CSS, JavaScript, Git)
INSERT INTO user_skills (user_id, skill_id) VALUES
('c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f', 9),
('c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f', 12),
('c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f', 13),
('c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f', 14),
('c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f', 15)
ON CONFLICT DO NOTHING;

-- Carlos DevOps (Docker, PostgreSQL, AWS, CI/CD, Git)
INSERT INTO user_skills (user_id, skill_id) VALUES
('d4e5f6a7-b8c9-0d1e-2f3a-4b5c6d7e8f9a', 8),
('d4e5f6a7-b8c9-0d1e-2f3a-4b5c6d7e8f9a', 9),
('d4e5f6a7-b8c9-0d1e-2f3a-4b5c6d7e8f9a', 11),
('d4e5f6a7-b8c9-0d1e-2f3a-4b5c6d7e8f9a', 16),
('d4e5f6a7-b8c9-0d1e-2f3a-4b5c6d7e8f9a', 17)
ON CONFLICT DO NOTHING;


-- 5. Inicializar CV de Estudiantes Demo

-- Jean Pool Experto CV
INSERT INTO cv_data (user_id, personal_info, education, experience, projects) VALUES
(
  'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
  '{"name": "Jean Pool Experto", "email": "jean.expert@utp.edu.pe", "phone": "+51 987 654 321", "linkedin": "linkedin.com/in/jean-expert", "github": "github.com/jeanexpert", "summary": "Estudiante avanzado de Sistemas con enfoque especialista en arquitectura Spring Boot, seguridad JWT y despliegues en contenedores."}',
  '[{"school": "Universidad Tecnológica del Perú", "degree": "Ingeniería de Sistemas", "startYear": "2022", "endYear": "2027"}]'::jsonb,
  '[{"company": "Software UTP Devs", "role": "Backend Intern", "description": "Desarrollo de servicios rest seguros y robustos en Java.", "startDate": "2025-01", "endDate": "2026-05"}]'::jsonb,
  '[{"title": "API Gateway Seguro", "description": "Enrutamiento modular con seguridad criptográfica JWT en Spring.", "link": "github.com/jeanexpert/gateway"}]'::jsonb
)
ON CONFLICT (user_id) DO NOTHING;

-- Luis Backend CV
INSERT INTO cv_data (user_id, personal_info, education, experience, projects) VALUES
(
  'b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e',
  '{"name": "Luis Backend", "email": "luis.backend@utp.edu.pe", "phone": "+51 912 345 678", "linkedin": "linkedin.com/in/luis-backend", "github": "github.com/luisbackend", "summary": "Desarrollador backend junior apasionado por escribir scripts limpios en Python, estructurar bases de datos complejas en SQL y optimizar APIs."}',
  '[{"school": "Universidad Tecnológica del Perú", "degree": "Ingeniería de Sistemas", "startYear": "2023", "endYear": "2028"}]'::jsonb,
  '[]'::jsonb,
  '[{"title": "Sistema Clínico", "description": "API REST en Django para gestionar historias clínicas electrónicas.", "link": "github.com/luisbackend/clinica"}]'::jsonb
)
ON CONFLICT (user_id) DO NOTHING;

-- Ana Frontend CV
INSERT INTO cv_data (user_id, personal_info, education, experience, projects) VALUES
(
  'c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f',
  '{"name": "Ana Front", "email": "ana.frontend@utp.edu.pe", "phone": "+51 988 888 888", "linkedin": "linkedin.com/in/ana-front", "github": "github.com/anafront", "summary": "Diseñadora y programadora frontend enfocada en crear interfaces UI/UX espectaculares y ágiles con React y Tailwind CSS."}',
  '[{"school": "Universidad Tecnológica del Perú", "degree": "Diseño y Desarrollo de Software", "startYear": "2024", "endYear": "2027"}]'::jsonb,
  '[{"company": "Studio Pixel UTP", "role": "Frontend Lead", "description": "Diseño e implementación de Landing Pages responsivas.", "startDate": "2025-06", "endDate": "2026-03"}]'::jsonb,
  '[{"title": "Dashboard Financiero", "description": "Interfaz interactiva con gráficos de barras y consumo de API en React.", "link": "github.com/anafront/dashboard"}]'::jsonb
)
ON CONFLICT (user_id) DO NOTHING;

-- Carlos DevOps CV
INSERT INTO cv_data (user_id, personal_info, education, experience, projects) VALUES
(
  'd4e5f6a7-b8c9-0d1e-2f3a-4b5c6d7e8f9a',
  '{"name": "Carlos DevOps", "email": "carlos.devops@utp.edu.pe", "phone": "+51 977 777 777", "linkedin": "linkedin.com/in/carlos-devops", "github": "github.com/carlosdev", "summary": "Apasionado por la automatización de procesos de software, contenedores y la orquestación segura en la nube con Docker y PostgreSQL."}',
  '[{"school": "Universidad Tecnológica del Perú", "degree": "Ingeniería de Redes", "startYear": "2022", "endYear": "2027"}]'::jsonb,
  '[]'::jsonb,
  '[{"title": "Infraestructura como Código", "description": "Automatización y dockerización de entornos de staging locales.", "link": "github.com/carlosdev/infra"}]'::jsonb
)
ON CONFLICT (user_id) DO NOTHING;


-- 6. Inicializar configuraciones estéticas de CV para Estudiantes Demo

-- Jean Pool (Ajustes elegantes Índigo)
INSERT INTO cv_settings (user_id, theme_color, font_family, spacing) VALUES
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', '#4F46E5', 'font-sans', 'space-y-5')
ON CONFLICT (user_id) DO NOTHING;

-- Luis (Ajustes sobrios pizarra)
INSERT INTO cv_settings (user_id, theme_color, font_family, spacing) VALUES
('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', '#475569', 'font-serif', 'space-y-4')
ON CONFLICT (user_id) DO NOTHING;

-- Ana (Ajustes modernos esmeralda)
INSERT INTO cv_settings (user_id, theme_color, font_family, spacing) VALUES
('c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f', '#10B981', 'font-sans', 'space-y-6')
ON CONFLICT (user_id) DO NOTHING;

-- Carlos (Ajustes técnicos mono)
INSERT INTO cv_settings (user_id, theme_color, font_family, spacing) VALUES
('d4e5f6a7-b8c9-0d1e-2f3a-4b5c6d7e8f9a', '#0F172A', 'font-mono', 'space-y-5')
ON CONFLICT (user_id) DO NOTHING;
