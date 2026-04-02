package kr.co.seoulit.reception.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "department")
@Getter
@Setter
@NoArgsConstructor
public class DepartmentEntity {

    @Id
    @Column(name = "dept_id")
    private String departmentId;

    @Column(name = "department_name", length = 50, nullable = false)
    private String departmentName;
}
