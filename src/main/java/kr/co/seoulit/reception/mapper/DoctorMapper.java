package kr.co.seoulit.reception.mapper;

import kr.co.seoulit.reception.dto.DoctorDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DoctorMapper {
    List<DoctorDTO> selectActiveDoctors(@Param("departmentId") String departmentId);

    String selectDoctorNameById(@Param("doctorId") String doctorId);

    int countActiveDoctors(
            @Param("doctorId") String doctorId,
            @Param("departmentId") String departmentId
    );
}
