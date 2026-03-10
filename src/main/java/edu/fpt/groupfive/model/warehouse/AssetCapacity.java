package edu.fpt.groupfive.model.warehouse;

import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class AssetCapacity {
    private Integer id;
    private Integer assetTypeId;
    private Integer capacityUnits;
}
