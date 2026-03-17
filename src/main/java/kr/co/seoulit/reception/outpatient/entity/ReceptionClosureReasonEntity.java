package kr.co.seoulit.reception.outpatient.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reception_closure_reason")
@Getter
@Setter
@NoArgsConstructor
public class ReceptionClosureReasonEntity {

    @Id
    @Column(name = "closure_reason_cd", length = 30)
    private String closureReasonCd;

    @Column(name = "closure_reason_name", length = 100, nullable = false)
    private String closureReasonName;

    @Column(name = "reason_group_cd", length = 30)
    private String reasonGroupCd;

    @Column(name = "usable_yn", length = 1, nullable = false)
    private String usableYn = "Y";

    @Column(name = "sort_order")
    private Integer sortOrder;
}
