CREATE TABLE IF NOT EXISTS skills (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100) UNIQUE NOT NULL,
    category VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS students (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    email               VARCHAR(150) UNIQUE NOT NULL,
    full_name           VARCHAR(150) NOT NULL,
    gpa                 NUMERIC(3,2) CHECK (gpa >= 0 AND gpa <= 4.0),
    grad_year           INT,
    work_auth_status    VARCHAR(30) NOT NULL DEFAULT 'OTHER',
    needs_sponsorship   BOOLEAN NOT NULL DEFAULT FALSE,
    resume_url          VARCHAR(300),
    preferred_locations VARCHAR(255),
    preferred_work_mode VARCHAR(20) DEFAULT 'ANY',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS student_skills (
    student_id  BIGINT,
    skill_id    BIGINT,
    proficiency SMALLINT DEFAULT 3,
    PRIMARY KEY (student_id, skill_id),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id)   REFERENCES skills(id)   ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS listings (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    title               VARCHAR(150) NOT NULL,
    company_name        VARCHAR(150) NOT NULL,
    description         TEXT,
    location            VARCHAR(120),
    work_mode           VARCHAR(20) NOT NULL DEFAULT 'ONSITE',
    min_gpa             NUMERIC(3,2) DEFAULT 0,
    sponsorship_offered BOOLEAN NOT NULL DEFAULT FALSE,
    role_type           VARCHAR(30) DEFAULT 'FULL_TIME',
    created_by          BIGINT,
    is_active           BOOLEAN DEFAULT TRUE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS listing_skills (
    listing_id BIGINT,
    skill_id   BIGINT,
    weight     SMALLINT DEFAULT 1,
    PRIMARY KEY (listing_id, skill_id),
    FOREIGN KEY (listing_id) REFERENCES listings(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id)   REFERENCES skills(id)   ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS match_results (
    student_id      BIGINT,
    listing_id      BIGINT,
    score           NUMERIC(5,2) NOT NULL,
    skill_score     NUMERIC(5,2),
    gpa_score       NUMERIC(5,2),
    auth_score      NUMERIC(5,2),
    matched_skills  TEXT,
    missing_skills  TEXT,
    computed_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (student_id, listing_id),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (listing_id) REFERENCES listings(id) ON DELETE CASCADE
);

CREATE INDEX idx_match_student_score ON match_results (student_id, score);

CREATE TABLE IF NOT EXISTS applications (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id  BIGINT,
    listing_id  BIGINT,
    status      VARCHAR(20) DEFAULT 'APPLIED',
    applied_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_student_listing (student_id, listing_id),
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (listing_id) REFERENCES listings(id)
);
