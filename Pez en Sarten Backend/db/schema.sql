    -- Esquema de Base de Datos para Impulsa Job
    -- Diseñado para PostgreSQL / Supabase

    -- 1. Tabla de Usuarios
    CREATE TABLE IF NOT EXISTS users (
        id UUID PRIMARY KEY,
        email VARCHAR(255) UNIQUE NOT NULL,
        full_name VARCHAR(255) NOT NULL,
        majors TEXT[] NOT NULL DEFAULT '{}'::TEXT[],
        verified BOOLEAN DEFAULT TRUE NOT NULL,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
    );

    -- 2. Tabla de Habilidades (Skills)
    CREATE TABLE IF NOT EXISTS skills (
        id SERIAL PRIMARY KEY,
        name VARCHAR(100) UNIQUE NOT NULL
    );

    -- 3. Tabla Intermedia Estudiante <-> Skills (User_Skills)
    CREATE TABLE IF NOT EXISTS user_skills (
        user_id UUID REFERENCES users(id) ON DELETE CASCADE,
        skill_id INT REFERENCES skills(id) ON DELETE CASCADE,
        PRIMARY KEY (user_id, skill_id)
    );

    -- 4. Tabla de Datos Estructurados del CV (JSONB)
    CREATE TABLE IF NOT EXISTS cv_data (
        user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
        personal_info JSONB NOT NULL DEFAULT '{}'::jsonb,
        education JSONB NOT NULL DEFAULT '[]'::jsonb,
        experience JSONB NOT NULL DEFAULT '[]'::jsonb,
        projects JSONB NOT NULL DEFAULT '[]'::jsonb
    );

    -- 5. Tabla de Configuración Estética del CV
    CREATE TABLE IF NOT EXISTS cv_settings (
        user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
        theme_color VARCHAR(10) NOT NULL DEFAULT '#4F46E5',
        font_family VARCHAR(50) NOT NULL DEFAULT 'font-sans',
        spacing VARCHAR(50) NOT NULL DEFAULT 'space-y-5'
    );

    -- 6. Tabla de Tareas (Retos Freelance Demo)
    CREATE TABLE IF NOT EXISTS tasks (
        id UUID PRIMARY KEY,
        title VARCHAR(255) NOT NULL,
        company_name VARCHAR(255) NOT NULL,
        company_logo VARCHAR(255),
        description TEXT NOT NULL,
        skills_required INTEGER[] NOT NULL DEFAULT '{}'::INTEGER[]
    );

    -- 7. Tabla de Resultados de Match con IA
    CREATE TABLE IF NOT EXISTS match_results (
        id UUID PRIMARY KEY,
        user_id UUID REFERENCES users(id) ON DELETE CASCADE,
        task_id UUID REFERENCES tasks(id) ON DELETE CASCADE,
        score_affinity INT NOT NULL,
        analysis TEXT NOT NULL,
        suggested_skills TEXT[] NOT NULL DEFAULT '{}'::TEXT[],
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
    );

    -- Índices para mejorar la búsqueda
    CREATE INDEX IF NOT EXISTS idx_skills_name ON skills(name);
    CREATE INDEX IF NOT EXISTS idx_tasks_skills ON tasks USING gin(skills_required);
