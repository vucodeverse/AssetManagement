package edu.fpt.groupfive.dto.request;

import edu.fpt.groupfive.common.Role;
import jakarta.validation.constraints.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserCreateRequest {
    @NotBlank(message = "Tên đăng nhập là bắt buộc")
    private String username;

    @NotBlank(message = "Mật khẩu là bắt buộc")
    @Size(min = 2, message = "Mật khẩu phải có ít nhất {min} ký tự")
    private String password;

    @NotBlank(message = "Tên là bắt buộc")
    private String firstName;

    @NotBlank(message = "Họ là bắt buộc")
    private String lastName;

    @Email(message = "Định dạng email không hợp lệ")
    private String email;

    private String phoneNumber;

    @NotNull(message = "Vai trò là bắt buộc")
    private Role role;

    @NotNull(message = "Phòng ban là bắt buộc")
    private Integer departmentId;

}
