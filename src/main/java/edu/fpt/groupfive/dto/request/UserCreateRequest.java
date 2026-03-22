package edu.fpt.groupfive.dto.request;

import edu.fpt.groupfive.common.Role;
import edu.fpt.groupfive.common.UserStatus;
import jakarta.validation.constraints.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserCreateRequest {
    @NotBlank(message = "Tên đăng nhập là bắt buộc")
    @Size(min = 4, max = 20, message = "Tên đăng nhập có độ dài từ {min} đến {max}")
    @Pattern(regexp = "^\\w+$", message = "Tên đăng nhập không được có kí tự đặc biệt")
    private String username;

    @NotBlank(message = "Mật khẩu là bắt buộc")
    @Size(min = 2, message = "Mật khẩu phải có ít nhất {min} ký tự")
    private String password;

    @NotBlank(message = "Tên là bắt buộc")

    private String firstName;

    @NotBlank(message = "Họ là bắt buộc")
    private String lastName;

    @Email(message = "Định dạng email không hợp lệ")
    @NotBlank(message = "Email là bắt buộc")
    private String email;

    @Pattern(
            regexp = "^0\\d{9}$",
            message = "Số điện thoại phải có 10 chữ số và có định dạng 0xxxxxxxxx"
    )
    private String phoneNumber;

    private UserStatus status = UserStatus.ACTIVE;

    @NotNull(message = "Vai trò là bắt buộc")
    private Role role;

    @NotNull(message = "Phòng ban là bắt buộc")
    private Integer departmentId;

}
