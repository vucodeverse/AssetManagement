package edu.fpt.groupfive.model.warehouse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssetPlacement {
    private Integer assetId;
    private Integer zoneId;
    private Integer placedBy;
    private LocalDateTime placedAt;
    private String note;
}
