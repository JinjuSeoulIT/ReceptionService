-- Minimal seed data for API testing
INSERT INTO department (department_id, department_name)
SELECT 1, 'Internal Medicine' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM department WHERE department_id = 1);

INSERT INTO department (department_id, department_name)
SELECT 2, 'Emergency' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM department WHERE department_id = 2);

INSERT INTO doctor (doctor_id, doctor_name, department_id)
SELECT 1, 'Dr. Kim', 1 FROM dual
WHERE NOT EXISTS (SELECT 1 FROM doctor WHERE doctor_id = 1);

INSERT INTO doctor (doctor_id, doctor_name, department_id)
SELECT 2, 'Dr. Lee', 2 FROM dual
WHERE NOT EXISTS (SELECT 1 FROM doctor WHERE doctor_id = 2);
