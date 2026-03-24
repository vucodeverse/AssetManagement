package edu.fpt.groupfive.dto.response;

import lombok.*;


import java.time.LocalDateTime;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DepartmentResponse {
    private Integer departmentId;
    private String departmentName;

    private String description;

    private LocalDateTime createdDate;

    private Integer managerId;

    private String managerName;
}
