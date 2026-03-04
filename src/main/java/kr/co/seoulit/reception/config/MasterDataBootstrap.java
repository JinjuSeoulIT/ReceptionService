package kr.co.seoulit.reception.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class MasterDataBootstrap implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        upsertDepartment(1L, "내과");
        upsertDepartment(2L, "외과");
        upsertDepartment(3L, "정형외과");
        upsertDepartment(4L, "신경외과");
        upsertDepartment(5L, "응급의학과");

        upsertDoctor(1L, "송태민", 1L);
        upsertDoctor(2L, "이현석", 2L);
        upsertDoctor(3L, "성숙희", 3L);
        upsertDoctor(4L, "최효정", 4L);
    }

    private void upsertDepartment(Long departmentId, String departmentName) {
        jdbcTemplate.update(
                "MERGE INTO department d " +
                        "USING (SELECT ? AS department_id, ? AS department_name FROM dual) s " +
                        "ON (d.department_id = s.department_id) " +
                        "WHEN MATCHED THEN UPDATE SET d.department_name = s.department_name " +
                        "WHEN NOT MATCHED THEN INSERT (department_id, department_name) VALUES (s.department_id, s.department_name)",
                departmentId,
                departmentName
        );
    }

    private void upsertDoctor(Long doctorId, String doctorName, Long departmentId) {
        jdbcTemplate.update(
                "MERGE INTO doctor d " +
                        "USING (SELECT ? AS doctor_id, ? AS doctor_name, ? AS department_id FROM dual) s " +
                        "ON (d.doctor_id = s.doctor_id) " +
                        "WHEN MATCHED THEN UPDATE SET d.doctor_name = s.doctor_name, d.department_id = s.department_id " +
                        "WHEN NOT MATCHED THEN INSERT (doctor_id, doctor_name, department_id) VALUES (s.doctor_id, s.doctor_name, s.department_id)",
                doctorId,
                doctorName,
                departmentId
        );
    }
}
