package edu.fpt.groupfive.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Department {
    private int departmentId    ;
    private String departmentName;
    private Integer managerUserId;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String description;
}