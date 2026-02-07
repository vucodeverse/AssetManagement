package edu.fpt.groupfive.model;

import edu.fpt.groupfive.common.Role;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Users {
    private Integer userId;

    private String username;

    private String passwordHash;

    private String fullName;

    private String phoneNumber;

    private String email;

    private String status;

    private Role role;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    //Foreign key of table Department
    private Integer departmentId;
}
