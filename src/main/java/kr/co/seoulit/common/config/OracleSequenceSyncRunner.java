package kr.co.seoulit.reception.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OracleSequenceSyncRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    private static final List<SequenceTarget> TARGETS = List.of(
            new SequenceTarget("SEQ_RECEPTION", "RECEPTION", "RECEPTION_ID"),
            new SequenceTarget("SEQ_RECEPTION_STATUS_HISTORY", "RECEPTION_STATUS_HISTORY", "STATUS_HISTORY_ID"),
            new SequenceTarget("SEQ_RECEPTION_OUTPATIENT_DTL", "RECEPTION_OUTPATIENT_DETAIL", "OUTPATIENT_DETAIL_ID"),
            new SequenceTarget("SEQ_RECEPTION_WAITING_QUEUE", "RECEPTION_WAITING_QUEUE", "WAITING_QUEUE_ID"),
            new SequenceTarget("SEQ_RECEPTION_QUAL_SNAP", "RECEPTION_QUALIFICATION_SNAP", "QUALIFICATION_SNAPSHOT_ID"),
            new SequenceTarget("SEQ_RECEPTION_QUAL_ITEM", "RECEPTION_QUALIFICATION_ITEM", "QUALIFICATION_ITEM_ID"),
            new SequenceTarget("SEQ_RECEPTION_CALL_HIS", "RECEPTION_CALL_HISTORY", "CALL_HISTORY_ID"),
            new SequenceTarget("SEQ_RECEPTION_VISIT_CLOSURE", "RECEPTION_VISIT_CLOSURE", "VISIT_CLOSURE_ID"),
            new SequenceTarget("SEQ_RECEPTION_VISIT_CLS_HIS", "RECEPTION_VISIT_CLOSURE_HIS", "VISIT_CLOSURE_HISTORY_ID"),
            new SequenceTarget("SEQ_RECEPTION_SETTLEMENT_SNAP", "RECEPTION_SETTLEMENT_SNAPSHOT", "SETTLEMENT_SNAPSHOT_ID"),
            new SequenceTarget("SEQ_RECEPTION_AUDIT", "RECEPTION_AUDIT", "RECEPTION_AUDIT_ID"),
            new SequenceTarget("SEQ_AUDIT_LOG", "AUDIT_LOG", "AUDIT_LOG_ID"),
            new SequenceTarget("SEQ_RESERVATION", "RESERVATION", "RESERVATION_ID"),
            new SequenceTarget("SEQ_RESERVATION_STATUS_HIS", "RESERVATION_STATUS_HISTORY", "RESERVATION_STATUS_HISTORY_ID"),
            new SequenceTarget("SEQ_RESERVATION_DOCTOR_SCH", "RESERVATION_DOCTOR_SCHEDULE", "SCHEDULE_ID"),
            new SequenceTarget("SEQ_RESERVATION_TIME_SLOT", "RESERVATION_TIME_SLOT", "TIME_SLOT_ID"),
            new SequenceTarget("SEQ_RESERVATION_BOOK_RULE", "RESERVATION_BOOKING_RULE", "BOOKING_RULE_ID"),
            new SequenceTarget("SEQ_RESV_TO_RECEPTION_HIS", "RESERVATION_TO_RECEPTION_HIS", "RESERVATION_RECEPTION_HIS_ID"),
            new SequenceTarget("SEQ_EMERGENCY_DETAIL", "EMERGENCY_DETAIL", "EMERGENCY_DETAIL_ID"),
            new SequenceTarget("SEQ_EMERGENCY_TRIAGE", "EMERGENCY_TRIAGE", "EMERGENCY_TRIAGE_ID"),
            new SequenceTarget("SEQ_INPATIENT_ADMISSION", "INPATIENT_ADMISSION", "INPATIENT_ADMISSION_ID"),
            new SequenceTarget("SEQ_INPT_ADM_DECISION", "INPATIENT_ADMISSION_DECISION", "DECISION_ID"),
            new SequenceTarget("SEQ_INPT_BED_ASSIGNMENT", "INPATIENT_BED_ASSIGNMENT", "BED_ASSIGNMENT_ID"),
            new SequenceTarget("SEQ_INPT_BED_ASSIGN_HIS", "INPATIENT_BED_ASSIGNMENT_HIS", "BED_ASSIGNMENT_HISTORY_ID"),
            new SequenceTarget("SEQ_INPT_ADM_AUDIT", "INPATIENT_ADMISSION_AUDIT", "INPATIENT_ADMISSION_AUDIT_ID")
    );

    @Override
    public void run(ApplicationArguments args) {
        for (SequenceTarget target : TARGETS) {
            align(target);
        }
    }

    private void align(SequenceTarget target) {
        try {
            Long maxId = jdbcTemplate.queryForObject(
                    "SELECT NVL(MAX(" + target.column + "), 0) FROM " + target.table,
                    Long.class
            );
            Long lastNumber = jdbcTemplate.queryForObject(
                    "SELECT LAST_NUMBER FROM USER_SEQUENCES WHERE SEQUENCE_NAME = ?",
                    Long.class,
                    target.sequence
            );
            if (maxId == null || lastNumber == null) {
                return;
            }

            long nextRequired = maxId + 1;
            if (lastNumber >= nextRequired) {
                return;
            }

            long increment = nextRequired - lastNumber;
            jdbcTemplate.execute("ALTER SEQUENCE " + target.sequence + " INCREMENT BY " + increment);
            jdbcTemplate.queryForObject("SELECT " + target.sequence + ".NEXTVAL FROM DUAL", Long.class);
            jdbcTemplate.execute("ALTER SEQUENCE " + target.sequence + " INCREMENT BY 1");
            log.info("Aligned sequence {} to {}", target.sequence, nextRequired);
        } catch (Exception ex) {
            log.warn("Sequence align skipped for {}: {}", target.sequence, ex.getMessage());
        }
    }

    private record SequenceTarget(String sequence, String table, String column) {
    }
}
