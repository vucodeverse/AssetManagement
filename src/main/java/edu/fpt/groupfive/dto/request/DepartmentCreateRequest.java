package edu.fpt.groupfive.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DepartmentCreateRequest {
    private Integer departmentId;
    @NotBlank (message = "Tên phòng ban không để trống!")
    @Pattern(regexp = "^[\\p{L}a-zA-Z0-9 .,_-]+$", message = "Tên phòng không được chứa kí tự đặc biệt")
    private String departmentName;
    private Integer managerId;
    @Pattern(regexp = "^[\\p{L}a-zA-Z0-9 .,_-]+$", message = "Miêu tả không được chứa kí tự đặc biệt")
    private String description;

    private String managerName;
}
