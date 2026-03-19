package edu.fpt.groupfive.dto.request.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AssetVolumeUpdateRequestDTO {

    @NotNull(message = "Loại tài sản không được để trống")
    private Integer assetTypeId;

    @NotNull(message = "Định mức không được để trống")
    @Min(value = 1, message = "Định mức phải lớn hơn 0")
    private Integer unitVolume;
}
