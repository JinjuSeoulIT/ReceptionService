package kr.co.seoulit.reception.service;

import kr.co.seoulit.reception.dto.DoctorDTO;
import kr.co.seoulit.reception.mapper.DoctorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorServiceImpl implements DoctorService {

    private final DoctorMapper doctorMapper;

    @Override
    public List<DoctorDTO> getActiveDoctors(String departmentId) {
        return doctorMapper.selectActiveDoctors(trimToNull(departmentId));
    }

    @Override
    public Optional<String> findDoctorName(String doctorId) {
        return Optional.ofNullable(
                doctorMapper.selectDoctorNameById(trimToNull(doctorId))
        );
    }

    @Override
    public void validateActiveDoctor(String doctorId, String departmentId) {
        String normalizedDoctorId = trimToNull(doctorId);
        if (normalizedDoctorId == null) {
            return;
        }

        int activeDoctorCount = doctorMapper.countActiveDoctors(
                normalizedDoctorId,
                trimToNull(departmentId)
        );
        if (activeDoctorCount <= 0) {
            throw new IllegalArgumentException("유효한 의사 ID가 아닙니다. doctorId=" + normalizedDoctorId);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
