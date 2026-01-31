package edu.fpt.groupfive.dto.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DepartmentCreateRequest {
    private String departmentName;
    private Integer managerId;
}
