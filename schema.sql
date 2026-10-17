-- =============================================================================
-- TSUBASA PLUS - Internship Management System (Quản lý Thực tập)
-- Database DDL Script for PostgreSQL (3NF Standard)
-- =============================================================================

-- Drop tables if exists (Cascade)
DROP TABLE IF EXISTS assessment_results CASCADE;
DROP TABLE IF EXISTS internship_assignments CASCADE;
DROP TABLE IF EXISTS round_criteria CASCADE;
DROP TABLE IF EXISTS assessment_rounds CASCADE;
DROP TABLE IF EXISTS evaluation_criteria CASCADE;
DROP TABLE IF EXISTS internship_phases CASCADE;
DROP TABLE IF EXISTS mentors CASCADE;
DROP TABLE IF EXISTS students CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Drop ENUM types if exists
DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS assignment_status CASCADE;

-- -----------------------------------------------------------------------------
-- ENUM TYPES
-- -----------------------------------------------------------------------------
CREATE TYPE user_role AS ENUM ('ADMIN', 'MENTOR', 'STUDENT');
CREATE TYPE assignment_status AS ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');

-- -----------------------------------------------------------------------------
-- AUTOMATIC UPDATED_AT TRIGGER FUNCTION
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- -----------------------------------------------------------------------------
-- 1. Table: users (Người dùng)
-- -----------------------------------------------------------------------------
CREATE TABLE users
(
    user_id       SERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    phone_number  VARCHAR(20),
    role          user_role    NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE
    ON users
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- -----------------------------------------------------------------------------
-- 2. Table: students (Thông tin Sinh viên)
-- -----------------------------------------------------------------------------
CREATE TABLE students
(
    student_id    INT PRIMARY KEY REFERENCES users (user_id) ON DELETE CASCADE,
    student_code  VARCHAR(20) NOT NULL UNIQUE,
    major         VARCHAR(100),
    class         VARCHAR(50),
    date_of_birth DATE,
    address       VARCHAR(255),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_students_updated_at
    BEFORE UPDATE
    ON students
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- -----------------------------------------------------------------------------
-- 3. Table: mentors (Thông tin Giáo viên Hướng dẫn)
-- -----------------------------------------------------------------------------
CREATE TABLE mentors
(
    mentor_id     INT PRIMARY KEY REFERENCES users (user_id) ON DELETE CASCADE,
    department    VARCHAR(100),
    academic_rank VARCHAR(50),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_mentors_updated_at
    BEFORE UPDATE
    ON mentors
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- -----------------------------------------------------------------------------
-- 4. Table: internship_phases (Giai đoạn Thực tập)
-- -----------------------------------------------------------------------------
CREATE TABLE internship_phases
(
    phase_id    SERIAL PRIMARY KEY,
    phase_name  VARCHAR(100) NOT NULL UNIQUE,
    start_date  DATE         NOT NULL,
    end_date    DATE         NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_phase_dates CHECK (end_date >= start_date)
);

CREATE TRIGGER update_internship_phases_updated_at
    BEFORE UPDATE
    ON internship_phases
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- -----------------------------------------------------------------------------
-- 5. Table: evaluation_criteria (Tiêu chí Đánh giá)
-- -----------------------------------------------------------------------------
CREATE TABLE evaluation_criteria
(
    criterion_id   SERIAL PRIMARY KEY,
    criterion_name VARCHAR(200)  NOT NULL UNIQUE,
    description    TEXT,
    max_score      NUMERIC(5, 2) NOT NULL CHECK (max_score > 0),
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_evaluation_criteria_updated_at
    BEFORE UPDATE
    ON evaluation_criteria
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- -----------------------------------------------------------------------------
-- 6. Table: assessment_rounds (Đợt Đánh giá)
-- -----------------------------------------------------------------------------
CREATE TABLE assessment_rounds
(
    round_id    SERIAL PRIMARY KEY,
    phase_id    INT          NOT NULL REFERENCES internship_phases (phase_id) ON DELETE CASCADE,
    round_name  VARCHAR(100) NOT NULL,
    start_date  DATE         NOT NULL,
    end_date    DATE         NOT NULL,
    description TEXT,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_round_dates CHECK (end_date >= start_date)
);

CREATE TRIGGER update_assessment_rounds_updated_at
    BEFORE UPDATE
    ON assessment_rounds
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- -----------------------------------------------------------------------------
-- 7. Table: round_criteria (Tiêu chí của Đợt Đánh giá)
-- -----------------------------------------------------------------------------
CREATE TABLE round_criteria
(
    round_criterion_id SERIAL PRIMARY KEY,
    round_id           INT           NOT NULL REFERENCES assessment_rounds (round_id) ON DELETE CASCADE,
    criterion_id       INT           NOT NULL REFERENCES evaluation_criteria (criterion_id) ON DELETE CASCADE,
    weight             NUMERIC(5, 2) NOT NULL CHECK (weight > 0 AND weight <= 1.00),
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_round_criterion UNIQUE (round_id, criterion_id)
);

CREATE TRIGGER update_round_criteria_updated_at
    BEFORE UPDATE
    ON round_criteria
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- -----------------------------------------------------------------------------
-- 8. Table: internship_assignments (Phân công Thực tập)
-- -----------------------------------------------------------------------------
CREATE TABLE internship_assignments
(
    assignment_id SERIAL PRIMARY KEY,
    student_id    INT               NOT NULL REFERENCES students (student_id) ON DELETE CASCADE,
    mentor_id     INT               NOT NULL REFERENCES mentors (mentor_id) ON DELETE CASCADE,
    phase_id      INT               NOT NULL REFERENCES internship_phases (phase_id) ON DELETE CASCADE,
    assigned_date TIMESTAMPTZ       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status        assignment_status NOT NULL DEFAULT 'PENDING',
    created_at    TIMESTAMPTZ       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_student_phase UNIQUE (student_id, phase_id)
);

CREATE TRIGGER update_internship_assignments_updated_at
    BEFORE UPDATE
    ON internship_assignments
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- -----------------------------------------------------------------------------
-- 9. Table: assessment_results (Kết quả Đánh giá)
-- -----------------------------------------------------------------------------
CREATE TABLE assessment_results
(
    result_id       SERIAL PRIMARY KEY,
    assignment_id   INT           NOT NULL REFERENCES internship_assignments (assignment_id) ON DELETE CASCADE,
    round_id        INT           NOT NULL REFERENCES assessment_rounds (round_id) ON DELETE CASCADE,
    criterion_id    INT           NOT NULL REFERENCES evaluation_criteria (criterion_id) ON DELETE RESTRICT,
    score           NUMERIC(5, 2) NOT NULL CHECK (score >= 0),
    comments        TEXT,
    evaluated_by    INT           NOT NULL REFERENCES users (user_id) ON DELETE RESTRICT,
    evaluation_date TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_assignment_round_criterion UNIQUE (assignment_id, round_id, criterion_id)
);

CREATE TRIGGER update_assessment_results_updated_at
    BEFORE UPDATE
    ON assessment_results
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- -----------------------------------------------------------------------------
-- INDEXES FOR PERFORMANCE OPTIMIZATION
-- -----------------------------------------------------------------------------
CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_students_code ON students (student_code);
CREATE INDEX idx_assignments_student ON internship_assignments (student_id);
CREATE INDEX idx_assignments_mentor ON internship_assignments (mentor_id);
CREATE INDEX idx_assignments_phase ON internship_assignments (phase_id);
CREATE INDEX idx_rounds_phase ON assessment_rounds (phase_id);
CREATE INDEX idx_assessment_results_assignment ON assessment_results (assignment_id);
CREATE INDEX idx_assessment_results_round ON assessment_results (round_id);
CREATE INDEX idx_results_criterion ON assessment_results (criterion_id);
CREATE INDEX idx_results_evaluator ON assessment_results (evaluated_by);
CREATE INDEX idx_round_criteria_criterion ON round_criteria (criterion_id);
