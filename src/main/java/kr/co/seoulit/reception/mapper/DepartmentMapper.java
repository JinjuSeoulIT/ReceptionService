package kr.co.seoulit.reception.mapper;

import kr.co.seoulit.reception.dto.DepartmentDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepartmentMapper {
    List<DepartmentDTO> selectActiveDepartments();

    String selectDepartmentNameById(@Param("departmentId") String departmentId);
}
