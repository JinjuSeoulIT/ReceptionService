-- Master seed data (department/doctor)
MERGE INTO department d
USING (SELECT 1 AS department_id, '내과' AS department_name FROM dual) s
ON (d.department_id = s.department_id)
WHEN MATCHED THEN UPDATE SET d.department_name = s.department_name
WHEN NOT MATCHED THEN INSERT (department_id, department_name)
VALUES (s.department_id, s.department_name);

MERGE INTO department d
USING (SELECT 2 AS department_id, '외과' AS department_name FROM dual) s
ON (d.department_id = s.department_id)
WHEN MATCHED THEN UPDATE SET d.department_name = s.department_name
WHEN NOT MATCHED THEN INSERT (department_id, department_name)
VALUES (s.department_id, s.department_name);

MERGE INTO department d
USING (SELECT 3 AS department_id, '정형외과' AS department_name FROM dual) s
ON (d.department_id = s.department_id)
WHEN MATCHED THEN UPDATE SET d.department_name = s.department_name
WHEN NOT MATCHED THEN INSERT (department_id, department_name)
VALUES (s.department_id, s.department_name);

MERGE INTO department d
USING (SELECT 4 AS department_id, '신경외과' AS department_name FROM dual) s
ON (d.department_id = s.department_id)
WHEN MATCHED THEN UPDATE SET d.department_name = s.department_name
WHEN NOT MATCHED THEN INSERT (department_id, department_name)
VALUES (s.department_id, s.department_name);

MERGE INTO doctor d
USING (SELECT 1 AS doctor_id, '송태민' AS doctor_name, 1 AS department_id FROM dual) s
ON (d.doctor_id = s.doctor_id)
WHEN MATCHED THEN UPDATE SET d.doctor_name = s.doctor_name, d.department_id = s.department_id
WHEN NOT MATCHED THEN INSERT (doctor_id, doctor_name, department_id)
VALUES (s.doctor_id, s.doctor_name, s.department_id);

MERGE INTO doctor d
USING (SELECT 2 AS doctor_id, '이현석' AS doctor_name, 2 AS department_id FROM dual) s
ON (d.doctor_id = s.doctor_id)
WHEN MATCHED THEN UPDATE SET d.doctor_name = s.doctor_name, d.department_id = s.department_id
WHEN NOT MATCHED THEN INSERT (doctor_id, doctor_name, department_id)
VALUES (s.doctor_id, s.doctor_name, s.department_id);

MERGE INTO doctor d
USING (SELECT 3 AS doctor_id, '성숙희' AS doctor_name, 3 AS department_id FROM dual) s
ON (d.doctor_id = s.doctor_id)
WHEN MATCHED THEN UPDATE SET d.doctor_name = s.doctor_name, d.department_id = s.department_id
WHEN NOT MATCHED THEN INSERT (doctor_id, doctor_name, department_id)
VALUES (s.doctor_id, s.doctor_name, s.department_id);

MERGE INTO doctor d
USING (SELECT 4 AS doctor_id, '최효정' AS doctor_name, 4 AS department_id FROM dual) s
ON (d.doctor_id = s.doctor_id)
WHEN MATCHED THEN UPDATE SET d.doctor_name = s.doctor_name, d.department_id = s.department_id
WHEN NOT MATCHED THEN INSERT (doctor_id, doctor_name, department_id)
VALUES (s.doctor_id, s.doctor_name, s.department_id);
