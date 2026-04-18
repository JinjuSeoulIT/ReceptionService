package kr.co.seoulit.reception.reservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reservation_booking_rule")
@Getter
@Setter
@NoArgsConstructor
public class ReservationBookingRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RESERVATION_BOOK_RULE")
    @SequenceGenerator(
            name = "SEQ_RESERVATION_BOOK_RULE",
            sequenceName = "SEQ_RESERVATION_BOOK_RULE",
            allocationSize = 1
    )
    @Column(name = "booking_rule_id")
    private Long bookingRuleId;

    @Column(name = "dept_id")
    private String deptId;

    @Column(name = "doctor_id")
    private String doctorId;

    @Column(name = "min_lead_min")
    private Integer minLeadMin;

    @Column(name = "max_lead_day")
    private Integer maxLeadDay;

    @Column(name = "overbook_allow_yn", length = 1, nullable = false)
    private String overbookAllowYn = "N";

    @Column(name = "cancel_deadline_min")
    private Integer cancelDeadlineMin;

    @Column(name = "priority_expr", length = 2000)
    private String priorityExpr;

    @Column(name = "active_yn", length = 1, nullable = false)
    private String activeYn = "Y";
}
