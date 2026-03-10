package edu.fpt.groupfive.dto.warehouse.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TicketMappedAssetDto {
    private Integer detailId;
    private Integer assetId;
    private Integer assetTypeId;
    private String assetName;
    private String assetTypeName;
}
