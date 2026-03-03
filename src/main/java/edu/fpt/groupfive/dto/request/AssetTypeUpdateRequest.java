package edu.fpt.groupfive.dto.request;

import edu.fpt.groupfive.common.AssetTypeClass;
import edu.fpt.groupfive.common.DepreciationMethod;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class AssetTypeUpdateRequest {

    private Integer typeId;

    @NotBlank(message = "Tên loại tài sản không được để trống")
    @Size(max = 100, message = "Tên loại tài sản không được vượt quá 100 ký tự")
    private String typeName;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;


    @NotNull(message = "Phân loại tài sản không được để trống")
    private AssetTypeClass typeClass;

    @NotNull(message = "Phương pháp khấu hao không được để trống")
    private DepreciationMethod defaultDepreciationMethod;

    @NotNull(message = "Thời gian sử dụng mặc định không được để trống")
    @Min(value = 1, message = "Thời gian sử dụng phải lớn hơn 0 tháng")
    @Max(value = 600, message = "Thời gian sử dụng không hợp lệ")
    private Integer defaultUsefulLifeMonths;

    @Size(max = 1000, message = "Thông số kỹ thuật không được vượt quá 1000 ký tự")
    private String specification;

    @Size(max = 200, message = "Model không được vượt quá 200 ký tự")
    private String model;

    @NotNull(message = "Danh mục không được để trống")
    private Integer categoryId;
}
