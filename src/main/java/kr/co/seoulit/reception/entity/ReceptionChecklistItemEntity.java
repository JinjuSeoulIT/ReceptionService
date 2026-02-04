package kr.co.seoulit.reception.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "RECEPTION_CHECKLIST_ITEM")
@Getter
@Setter
public class ReceptionChecklistItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RECEPTION_CHECKLIST_ITEM_SEQ")
    @SequenceGenerator(
            name = "RECEPTION_CHECKLIST_ITEM_SEQ",
            sequenceName = "RECEPTION_CHECKLIST_ITEM_SEQ",
            allocationSize = 1
    )
    @Column(name = "CHECKLIST_ID")
    private Long checklistId;

    @Column(name = "CHECK_DATE", nullable = false)
    private LocalDate checkDate;

    @Column(name = "LABEL", length = 200, nullable = false)
    private String label;

    @Column(name = "DONE", nullable = false)
    private boolean done;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder;
}
