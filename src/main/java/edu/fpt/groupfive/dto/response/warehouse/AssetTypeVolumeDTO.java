package edu.fpt.groupfive.dto.response.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetTypeVolumeDTO {
    private Integer assetTypeId;
    private String typeName;
    private Integer unitVolume;   // đơn vị sức chứa chiếm dụng, NULL = chưa cấu hình
    private String categoryName;
}
