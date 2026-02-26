package edu.fpt.groupfive.dto.request;

import edu.fpt.groupfive.common.Role;
import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserUpdateRequest {
    private Integer userId;

    private String username;

    private String password;

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private Role role;

    private String status;

    private Integer departmentId;
}
