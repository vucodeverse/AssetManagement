package edu.fpt.groupfive.model.warehouse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AssetLocation {
    private Integer assetId;
    private Integer zoneId;
    private Integer lastTicketId;
}
