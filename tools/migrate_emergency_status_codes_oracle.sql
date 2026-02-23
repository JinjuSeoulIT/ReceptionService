-- Emergency status code migration (Oracle / STM schema)
-- Canonical emergency statuses: WAITING, TRIAGE, IN_PROGRESS, OBSERVATION, COMPLETED, TRANSFERRED, ON_HOLD, CANCELED

UPDATE reception
   SET status = 'TRIAGE'
 WHERE visit_type = 'EMERGENCY'
   AND status = 'CALLED';

UPDATE reception
   SET status = 'OBSERVATION'
 WHERE visit_type = 'EMERGENCY'
   AND status = 'PAYMENT_WAIT';

UPDATE reception
   SET status = 'TRANSFERRED'
 WHERE visit_type = 'EMERGENCY'
   AND status = 'INACTIVE';

UPDATE reception_status_history h
   SET from_status = 'TRIAGE'
 WHERE h.from_status = 'CALLED'
   AND EXISTS (
       SELECT 1 FROM reception r
        WHERE r.reception_id = h.reception_id
          AND r.visit_type = 'EMERGENCY'
   );

UPDATE reception_status_history h
   SET from_status = 'OBSERVATION'
 WHERE h.from_status = 'PAYMENT_WAIT'
   AND EXISTS (
       SELECT 1 FROM reception r
        WHERE r.reception_id = h.reception_id
          AND r.visit_type = 'EMERGENCY'
   );

UPDATE reception_status_history h
   SET from_status = 'TRANSFERRED'
 WHERE h.from_status = 'INACTIVE'
   AND EXISTS (
       SELECT 1 FROM reception r
        WHERE r.reception_id = h.reception_id
          AND r.visit_type = 'EMERGENCY'
   );

UPDATE reception_status_history h
   SET to_status = 'TRIAGE'
 WHERE h.to_status = 'CALLED'
   AND EXISTS (
       SELECT 1 FROM reception r
        WHERE r.reception_id = h.reception_id
          AND r.visit_type = 'EMERGENCY'
   );

UPDATE reception_status_history h
   SET to_status = 'OBSERVATION'
 WHERE h.to_status = 'PAYMENT_WAIT'
   AND EXISTS (
       SELECT 1 FROM reception r
        WHERE r.reception_id = h.reception_id
          AND r.visit_type = 'EMERGENCY'
   );

UPDATE reception_status_history h
   SET to_status = 'TRANSFERRED'
 WHERE h.to_status = 'INACTIVE'
   AND EXISTS (
       SELECT 1 FROM reception r
        WHERE r.reception_id = h.reception_id
          AND r.visit_type = 'EMERGENCY'
   );

ALTER TABLE RECEPTION_STATUS_HISTORY DROP CONSTRAINT CHK_RECEPTION_STATUS_HI_349CDE;
ALTER TABLE RECEPTION_STATUS_HISTORY DROP CONSTRAINT CHK_RECEPTION_STATUS_HI_AA1246;

ALTER TABLE RECEPTION_STATUS_HISTORY ADD CONSTRAINT CHK_RECEPTION_STATUS_HI_349CDE
CHECK (
  from_status IN (
    'WAITING', 'CALLED', 'TRIAGE', 'IN_PROGRESS',
    'COMPLETED', 'PAYMENT_WAIT', 'OBSERVATION',
    'ON_HOLD', 'CANCELED', 'INACTIVE', 'TRANSFERRED'
  )
);

ALTER TABLE RECEPTION_STATUS_HISTORY ADD CONSTRAINT CHK_RECEPTION_STATUS_HI_AA1246
CHECK (
  to_status IN (
    'WAITING', 'CALLED', 'TRIAGE', 'IN_PROGRESS',
    'COMPLETED', 'PAYMENT_WAIT', 'OBSERVATION',
    'ON_HOLD', 'CANCELED', 'INACTIVE', 'TRANSFERRED'
  )
);

COMMIT;
