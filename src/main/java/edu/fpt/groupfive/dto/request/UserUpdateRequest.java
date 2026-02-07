package edu.fpt.groupfive.dto.request;

import edu.fpt.groupfive.common.Role;
import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserUpdateRequest {
    private Integer userId;

    private String password;

    private String fullName;

    private String email;

    private String phoneNumber;

    private Role role;

    private String status;

    private Integer departmentId;
}
