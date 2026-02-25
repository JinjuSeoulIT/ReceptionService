-- cleanup_outpatient_reception.sql
-- Purpose:
--   Remove outpatient rows from reception and related reception_status_history.
--   Supports both visit_type='OUTPATIENT' and Korean code visit_type='외래'.
--   Keep a point-in-time backup before delete.
--
-- Usage (SQL*Plus):
--   sqlplus USER/PASSWORD@DB @tools/cleanup_outpatient_reception.sql

SET SERVEROUTPUT ON
SET FEEDBACK ON
SET DEFINE OFF
WHENEVER SQLERROR EXIT FAILURE ROLLBACK

DECLARE
  v_reception_cnt NUMBER := 0;
  v_history_cnt   NUMBER := 0;
BEGIN
  SELECT COUNT(*)
    INTO v_reception_cnt
    FROM reception
   WHERE visit_type = 'OUTPATIENT'
      OR visit_type = UNISTR('\C678\B798');

  SELECT COUNT(*)
    INTO v_history_cnt
    FROM reception_status_history h
   WHERE EXISTS (
         SELECT 1
           FROM reception r
          WHERE r.reception_id = h.reception_id
            AND (
                 r.visit_type = 'OUTPATIENT'
                 OR r.visit_type = UNISTR('\C678\B798')
            )
       );

  DBMS_OUTPUT.PUT_LINE('OUTPATIENT reception target rows: ' || v_reception_cnt);
  DBMS_OUTPUT.PUT_LINE('OUTPATIENT history target rows: ' || v_history_cnt);

  -- Backup tables (change suffix date if you run again).
  EXECUTE IMMEDIATE 'CREATE TABLE rec_bak_20260224 AS SELECT * FROM reception';
  EXECUTE IMMEDIATE 'CREATE TABLE rec_hist_bak_20260224 AS SELECT * FROM reception_status_history';

  DELETE FROM reception_status_history h
   WHERE EXISTS (
         SELECT 1
           FROM reception r
          WHERE r.reception_id = h.reception_id
            AND r.visit_type = 'OUTPATIENT'
       );
  DBMS_OUTPUT.PUT_LINE('Deleted history rows: ' || SQL%ROWCOUNT);

  DELETE FROM reception
   WHERE visit_type = 'OUTPATIENT'
      OR visit_type = UNISTR('\C678\B798');
  DBMS_OUTPUT.PUT_LINE('Deleted reception rows: ' || SQL%ROWCOUNT);

  COMMIT;
  DBMS_OUTPUT.PUT_LINE('COMMIT complete');
END;
/

PROMPT ===== Verify =====
SELECT COUNT(*) AS outpatient_reception_left
  FROM reception
 WHERE visit_type = 'OUTPATIENT'
    OR visit_type = UNISTR('\C678\B798');

SELECT COUNT(*) AS outpatient_history_left
  FROM reception_status_history h
 WHERE EXISTS (
       SELECT 1
         FROM reception r
        WHERE r.reception_id = h.reception_id
          AND (
               r.visit_type = 'OUTPATIENT'
               OR r.visit_type = UNISTR('\C678\B798')
          )
     );
