/*------------------------------------------------------------------------------
- Legacy numeric doctor/department seed removed.
- Current master data source:
    * CMH.STAFF_DEPARTMENT.DEPARTMENT_ID / DEPARTMENT_NAME
    * CMH.STAFF.STAFF_ID / FULL_NAME / STAFF_DEPARTMENT_ID
- DOCTOR_ID in current reception/reservation flows is a CMH.STAFF.STAFF_ID
  such as DOC-2026-0017, so the old numeric sample seed is no longer valid.
- This script is now a verification helper for Oracle 11g environments.
------------------------------------------------------------------------------*/

SET PAGESIZE 200;
SET LINESIZE 200;

PROMPT [1/2] Departments that currently have active doctors

SELECT
    d.department_id,
    d.department_name,
    COUNT(s.staff_id) AS active_doctor_count
FROM CMH.STAFF_DEPARTMENT d
JOIN CMH.STAFF s
  ON s.staff_department_id = d.department_id
 AND s.staff_id LIKE 'DOC-%'
 AND UPPER(TRIM(NVL(s.employment_status, 'ACTIVE'))) = 'ACTIVE'
GROUP BY d.department_id, d.department_name
ORDER BY d.department_name;

PROMPT [2/2] Active doctors from CMH.STAFF

SELECT
    s.staff_id        AS doctor_id,
    s.full_name       AS doctor_name,
    s.staff_department_id AS department_id
FROM CMH.STAFF s
WHERE s.staff_id LIKE 'DOC-%'
  AND UPPER(TRIM(NVL(s.employment_status, 'ACTIVE'))) = 'ACTIVE'
ORDER BY s.staff_department_id, s.full_name;
