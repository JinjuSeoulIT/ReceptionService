package kr.co.seoulit.reception.service;

import kr.co.seoulit.reception.dto.DoctorDTO;

import java.util.List;
import java.util.Optional;

public interface DoctorService {
    List<DoctorDTO> getActiveDoctors(String departmentId);

    Optional<String> findDoctorName(String doctorId);

    void validateActiveDoctor(String doctorId, String departmentId);
}
