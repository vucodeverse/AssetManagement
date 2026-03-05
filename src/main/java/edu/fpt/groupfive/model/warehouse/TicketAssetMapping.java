package edu.fpt.groupfive.model.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketAssetMapping {
    private Integer id;
    private Integer detailId;
    private Integer assetId;
}
