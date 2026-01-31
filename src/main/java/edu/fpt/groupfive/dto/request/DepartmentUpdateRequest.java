package edu.fpt.groupfive.dto.request;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DepartmentUpdateRequest {
    private Integer departmentId;

    private String departmentName;

    private String status;

    private Integer managerId;
}
