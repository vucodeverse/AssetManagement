package edu.fpt.groupfive.dto.response.dashboardadmin;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDTO {
    private String departmentId;
    private String departmentName;
    private int staffCount;
}
