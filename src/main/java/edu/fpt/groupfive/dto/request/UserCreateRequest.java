package edu.fpt.groupfive.dto.request;

import edu.fpt.groupfive.common.Role;
import jakarta.validation.constraints.*;
import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserCreateRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 2, message = "Password must be at least {min} characters")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;

    @NotNull(message = "Role is required")
    private Role role;

    @NotNull(message = "Department is required")
    private Integer departmentId;

}
