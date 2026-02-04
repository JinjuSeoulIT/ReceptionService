package kr.co.seoulit.reception.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReceptionChecklistItemRequest {
    private LocalDate checkDate;
    private String label;
    private boolean done;
    private Integer sortOrder;
}
