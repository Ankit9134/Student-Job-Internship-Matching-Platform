-- Skills
INSERT INTO skills (name, category) VALUES
 ('Java', 'language'), ('Python', 'language'), ('JavaScript', 'language'), ('TypeScript', 'language'),
 ('React', 'framework'), ('Angular', 'framework'), ('Spring Boot', 'framework'), ('Node.js', 'framework'),
 ('SQL', 'tool'), ('PostgreSQL', 'tool'), ('Docker', 'tool'), ('AWS', 'tool'),
 ('GraphQL', 'tool'), ('Git', 'tool'), ('Communication', 'soft-skill'), ('Teamwork', 'soft-skill')
ON CONFLICT (name) DO NOTHING;

-- Demo student
INSERT INTO students (id, email, full_name, gpa, grad_year, work_auth_status, needs_sponsorship, preferred_work_mode)
VALUES (1, 'jane.doe@university.edu', 'Jane Doe', 3.6, 2027, 'F1_OPT', TRUE, 'REMOTE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO student_skills (student_id, skill_id, proficiency)
SELECT 1, id, 4 FROM skills WHERE name IN ('React', 'TypeScript', 'SQL', 'Git', 'Communication')
ON CONFLICT DO NOTHING;

-- Sample listings
INSERT INTO listings (id, title, company_name, description, location, work_mode, min_gpa, sponsorship_offered, role_type)
VALUES
 (1, 'Frontend Engineering Intern', 'Acme Corp', 'Build UI features with React and TypeScript.', 'Remote', 'REMOTE', 3.0, TRUE, 'INTERNSHIP'),
 (2, 'Backend Developer', 'Globex Inc', 'Spring Boot services for a payments platform.', 'New York, NY', 'HYBRID', 3.3, FALSE, 'FULL_TIME'),
 (3, 'Data Analyst Intern', 'Initech', 'SQL-heavy reporting and dashboarding work.', 'Austin, TX', 'ONSITE', 2.8, TRUE, 'INTERNSHIP'),
 (4, 'Full Stack Engineer', 'Umbrella Labs', 'Angular + Node.js product engineering.', 'Remote', 'REMOTE', 3.5, TRUE, 'FULL_TIME'),
 (5, 'Cloud Infrastructure Intern', 'Stark Industries', 'AWS + Docker infra automation.', 'Seattle, WA', 'HYBRID', 3.2, FALSE, 'INTERNSHIP')
ON CONFLICT (id) DO NOTHING;

INSERT INTO listing_skills (listing_id, skill_id, weight)
SELECT 1, id, 2 FROM skills WHERE name IN ('React', 'TypeScript')
ON CONFLICT DO NOTHING;
INSERT INTO listing_skills (listing_id, skill_id, weight)
SELECT 1, id, 1 FROM skills WHERE name IN ('GraphQL', 'Git')
ON CONFLICT DO NOTHING;

INSERT INTO listing_skills (listing_id, skill_id, weight)
SELECT 2, id, 2 FROM skills WHERE name IN ('Java', 'Spring Boot', 'SQL')
ON CONFLICT DO NOTHING;

INSERT INTO listing_skills (listing_id, skill_id, weight)
SELECT 3, id, 2 FROM skills WHERE name IN ('SQL', 'PostgreSQL')
ON CONFLICT DO NOTHING;
INSERT INTO listing_skills (listing_id, skill_id, weight)
SELECT 3, id, 1 FROM skills WHERE name IN ('Python')
ON CONFLICT DO NOTHING;

INSERT INTO listing_skills (listing_id, skill_id, weight)
SELECT 4, id, 2 FROM skills WHERE name IN ('Angular', 'Node.js', 'TypeScript')
ON CONFLICT DO NOTHING;

INSERT INTO listing_skills (listing_id, skill_id, weight)
SELECT 5, id, 2 FROM skills WHERE name IN ('AWS', 'Docker')
ON CONFLICT DO NOTHING;

-- Keep sequences ahead of the manually-inserted ids above
SELECT setval('students_id_seq', (SELECT MAX(id) FROM students));
SELECT setval('listings_id_seq', (SELECT MAX(id) FROM listings));
