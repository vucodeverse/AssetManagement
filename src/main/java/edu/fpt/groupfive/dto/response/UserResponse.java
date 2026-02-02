package edu.fpt.groupfive.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Integer userId;

    private String username;

    private String fullName;

    private String phoneNumber;

    private String email;

    private String status;

    private String role;

    private LocalDateTime createdDate;

    //Foreign key of table Department
    private Integer departmentId;
}
