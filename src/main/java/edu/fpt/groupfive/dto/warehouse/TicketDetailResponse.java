package edu.fpt.groupfive.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDetailResponse {
    private Integer id;
    private Integer ticketId;
    private Integer assetTypeId;
    private Integer expectedQuantity;
    private Integer actualQuantity;
    private String note;
}
