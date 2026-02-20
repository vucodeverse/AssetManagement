package edu.fpt.groupfive.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryUpdateRequest {
    private Integer categoryId;
    @NotBlank(message = "Tên danh mục không được để trống")
    private String categoryName;
    private String description;
    private String status;
}
