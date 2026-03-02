package edu.fpt.groupfive.dto.request;

import edu.fpt.groupfive.common.Role;
import jakarta.validation.constraints.*;
import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserUpdateRequest {
    private Integer userId;

    private String username;

    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phoneNumber;

    @NotNull(message = "Role is required")
    private Role role;

    private String status;

    @NotNull(message = "Department is required")
    private Integer departmentId;
}
