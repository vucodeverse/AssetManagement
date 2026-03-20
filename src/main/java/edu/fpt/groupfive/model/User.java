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
public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String status;
    private String role;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private int departmentId;
}