package edu.fpt.groupfive.dto.response;

import edu.fpt.groupfive.common.Role;
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

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private String status;

    private Role role;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    //Foreign key of table Department
    private Integer departmentId;
}
