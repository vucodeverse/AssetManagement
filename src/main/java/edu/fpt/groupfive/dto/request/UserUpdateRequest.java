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

    @NotBlank(message = "Tên là bắt buộc")
    private String firstName;

    @NotBlank(message = "Họ là bắt buộc")
    private String lastName;

    @Email(message = "Định dạng email không hợp lệ")
    private String email;

    @NotBlank(message = "Số điện thoại là bắt buộc")
    private String phoneNumber;

    @NotNull(message = "Vai trò là bắt buộc")
    private Role role;

    private String status;

    @NotNull(message = "Phòng ban là bắt buộc")
    private Integer departmentId;
}
