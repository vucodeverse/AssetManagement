package edu.fpt.groupfive.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCreateRequest {
    private Integer warehouseId;
    private String ticketType; // IN, OUT
    private Integer createdBy;
    private String note;
    private List<TicketDetailRequest> details;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketDetailRequest {
        private Integer assetTypeId;
        private Integer expectedQuantity;
        private String note;
    }
}
