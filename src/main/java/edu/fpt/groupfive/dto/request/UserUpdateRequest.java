package edu.fpt.groupfive.dto.request;

import edu.fpt.groupfive.common.Role;
import edu.fpt.groupfive.common.UserStatus;
import jakarta.validation.constraints.*;
import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserUpdateRequest {
    private Integer userId;

    private String username;

    @NotBlank(message = "Mật khẩu là bắt buộc")
    @Size(min = 2, message = "Mật khẩu phải có ít nhất {min} ký tự")
    private String password;


    @NotBlank(message = "Tên là bắt buộc")
    @Pattern(
            regexp = "^(\\p{Lu}\\p{Ll}+)(\\s\\p{Lu}\\p{Ll}+)*$",
            message = "Mỗi từ phải bắt đầu hoa, chỉ chứa chữ, không số/ký tự đặc biệt, không khoảng trắng thừa"
    )
    private String firstName;

    @NotBlank(message = "Họ là bắt buộc")
    @Pattern(
            regexp = "^(\\p{Lu}\\p{Ll}+)(\\s\\p{Lu}\\p{Ll}+)*$",
            message = "Mỗi từ phải bắt đầu hoa, chỉ chứa chữ, không số/ký tự đặc biệt, không khoảng trắng thừa"
    )
    private String lastName;

    @Email(message = "Định dạng email không hợp lệ")
    @NotBlank(message = "Email là bắt buộc")
    private String email;

    @Pattern(
            regexp = "^0\\d{9}$",
            message = "Số điện thoại phải có 10 chữ số và bắt đầu bằng 0"
    )
    private String phoneNumber;

    @NotNull(message = "Vai trò là bắt buộc")
    private Role role;

    private UserStatus status;

    @NotNull(message = "Phòng ban là bắt buộc")
    private Integer departmentId;
}