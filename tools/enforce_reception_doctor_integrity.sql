/*------------------------------------------------------------------------------
- Strengthen HOSPITAL.RECEPTION.DOCTOR_ID integrity
- Only active doctors in CMH.STAFF with STAFF_ID like DOC-% are allowed
- If DEPARTMENT_ID is present, it must match CMH.STAFF.STAFF_DEPARTMENT_ID
------------------------------------------------------------------------------*/

SET SERVEROUTPUT ON;

PROMPT [1/4] Check invalid reception doctor rows before enforcement

SELECT COUNT(*) AS invalid_doctor_rows
FROM HOSPITAL.RECEPTION r
WHERE r.doctor_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM CMH.STAFF s
    WHERE s.staff_id = TRIM(TO_CHAR(r.doctor_id))
      AND s.staff_id LIKE 'DOC-%'
      AND UPPER(TRIM(NVL(s.employment_status, 'ACTIVE'))) = 'ACTIVE'
      AND (
        r.department_id IS NULL
        OR s.staff_department_id = TRIM(TO_CHAR(r.department_id))
      )
  );

DECLARE
  v_invalid_count NUMBER := 0;
BEGIN
  SELECT COUNT(*)
    INTO v_invalid_count
    FROM HOSPITAL.RECEPTION r
   WHERE r.doctor_id IS NOT NULL
     AND NOT EXISTS (
       SELECT 1
         FROM CMH.STAFF s
        WHERE s.staff_id = TRIM(TO_CHAR(r.doctor_id))
          AND s.staff_id LIKE 'DOC-%'
          AND UPPER(TRIM(NVL(s.employment_status, 'ACTIVE'))) = 'ACTIVE'
          AND (
            r.department_id IS NULL
            OR s.staff_department_id = TRIM(TO_CHAR(r.department_id))
          )
     );

  IF v_invalid_count > 0 THEN
    RAISE_APPLICATION_ERROR(
      -20060,
      'Cannot enforce reception doctor integrity. Found '
      || v_invalid_count
      || ' invalid row(s) in HOSPITAL.RECEPTION.'
    );
  END IF;
END;
/

PROMPT [2/4] Add CHECK constraint for DOCTOR_ID format

DECLARE
  v_exists NUMBER := 0;
BEGIN
  SELECT COUNT(*)
    INTO v_exists
    FROM ALL_CONSTRAINTS
   WHERE OWNER = 'HOSPITAL'
     AND TABLE_NAME = 'RECEPTION'
     AND CONSTRAINT_NAME = 'CK_RECEPTION_DOCTOR_ID_FMT';

  IF v_exists = 0 THEN
    EXECUTE IMMEDIATE q'[
      ALTER TABLE HOSPITAL.RECEPTION
      ADD CONSTRAINT CK_RECEPTION_DOCTOR_ID_FMT
      CHECK (DOCTOR_ID IS NULL OR TRIM(DOCTOR_ID) LIKE 'DOC-%')
    ]';
    DBMS_OUTPUT.PUT_LINE('Added CK_RECEPTION_DOCTOR_ID_FMT');
  ELSE
    DBMS_OUTPUT.PUT_LINE('CK_RECEPTION_DOCTOR_ID_FMT already exists');
  END IF;
END;
/

PROMPT [3/4] Create validation trigger

CREATE OR REPLACE TRIGGER HOSPITAL.TRG_RECEPTION_VALIDATE_DOCTOR
BEFORE INSERT OR UPDATE OF DOCTOR_ID, DEPARTMENT_ID
ON HOSPITAL.RECEPTION
FOR EACH ROW
DECLARE
  v_count         NUMBER := 0;
  v_doctor_id     VARCHAR2(30);
  v_department_id VARCHAR2(30);
BEGIN
  v_doctor_id := TRIM(TO_CHAR(:NEW.DOCTOR_ID));
  v_department_id := TRIM(TO_CHAR(:NEW.DEPARTMENT_ID));

  IF v_doctor_id IS NULL THEN
    RETURN;
  END IF;

  SELECT COUNT(*)
    INTO v_count
    FROM CMH.STAFF s
   WHERE s.staff_id = v_doctor_id
     AND s.staff_id LIKE 'DOC-%'
     AND UPPER(TRIM(NVL(s.employment_status, 'ACTIVE'))) = 'ACTIVE'
     AND (
       v_department_id IS NULL
       OR s.staff_department_id = v_department_id
     );

  IF v_count = 0 THEN
    RAISE_APPLICATION_ERROR(
      -20061,
      'DOCTOR_ID must reference an active doctor in CMH.STAFF and match DEPARTMENT_ID.'
    );
  END IF;
END;
/

SHOW ERRORS TRIGGER HOSPITAL.TRG_RECEPTION_VALIDATE_DOCTOR;

PROMPT [4/4] Verify installed objects

SELECT constraint_name, status
FROM ALL_CONSTRAINTS
WHERE OWNER = 'HOSPITAL'
  AND TABLE_NAME = 'RECEPTION'
  AND CONSTRAINT_NAME = 'CK_RECEPTION_DOCTOR_ID_FMT';

SELECT trigger_name, status
FROM ALL_TRIGGERS
WHERE OWNER = 'HOSPITAL'
  AND TABLE_NAME = 'RECEPTION'
  AND TRIGGER_NAME = 'TRG_RECEPTION_VALIDATE_DOCTOR';
