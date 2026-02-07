package edu.fpt.groupfive.model;

import lombok.*;

import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Department {
    private Integer departmentId;

    private String departmentName;

    private String description;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    // ACTIVE or INACTIVE
    private String status;

    //Foreign user_id of Table Users
    private Integer managerId;

}
