package kr.co.seoulit.reception.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuNode {
    private Long id;
    private String code;
    private String name;
    private String path;
    private String icon;
    private Integer sortOrder;
    private List<MenuNode> children;
}
