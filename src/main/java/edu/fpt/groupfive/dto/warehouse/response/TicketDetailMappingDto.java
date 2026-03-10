package edu.fpt.groupfive.dto.warehouse.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TicketDetailMappingDto {
    private Integer detailId;
    private Integer assetTypeId;
    private String assetTypeName;
    private Integer quantityRequested;
    private Integer quantityMapped;
    private String note;
}
