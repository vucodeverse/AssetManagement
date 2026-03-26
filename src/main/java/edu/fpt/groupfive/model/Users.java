package edu.fpt.groupfive.model;

import edu.fpt.groupfive.common.Role;
import edu.fpt.groupfive.common.UserStatus;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
public class Users {
    private Integer userId;

    private String username;

    private String passwordHash;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String email;

    private UserStatus status;

    private Role role;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    //Foreign key of table Department
    private Integer departmentId;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
