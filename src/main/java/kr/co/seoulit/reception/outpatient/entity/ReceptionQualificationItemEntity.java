package kr.co.seoulit.reception.outpatient.entity;

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
@Table(name = "reception_qualification_item")
@Getter
@Setter
@NoArgsConstructor
public class ReceptionQualificationItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_RECEPTION_QUAL_ITEM")
    @SequenceGenerator(name = "SEQ_RECEPTION_QUAL_ITEM", sequenceName = "SEQ_RECEPTION_QUAL_ITEM", allocationSize = 1)
    @Column(name = "qualification_item_id")
    private Long qualificationItemId;

    @Column(name = "qualification_snapshot_id", nullable = false)
    private Long qualificationSnapshotId;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(name = "item_value", length = 500)
    private String itemValue;

    @Column(name = "item_status_cd", length = 30)
    private String itemStatusCd;

    @Column(name = "display_order")
    private Integer displayOrder;
}
