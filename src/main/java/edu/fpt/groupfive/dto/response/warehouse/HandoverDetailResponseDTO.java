package edu.fpt.groupfive.dto.response.warehouse;

import edu.fpt.groupfive.dto.response.AllocationRequestResponse;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import edu.fpt.groupfive.model.warehouse.WhReceipt;

@Data
@Builder
public class HandoverDetailResponseDTO {
    private Integer handoverId;
    private String fromDepartmentName;
    private String toDepartmentName;
    private String status;
    private List<HandoverItemDTO> items;
    private AllocationRequestResponse allocationRequest;
    private List<RequestedItemDTO> requestedItems;
    private List<WhReceipt> receipts;

    @Data
    @Builder
    public static class HandoverItemDTO {
        private Integer assetId;
        private String assetCode;
        private String assetName;
        private String assetTypeName;
        private boolean isScanned;
    }

    @Data
    @Builder
    public static class RequestedItemDTO {
        private String assetTypeName;
        private Integer requestedQuantity;
        private Integer allocatedQuantity;
        private Integer stagedQuantity; // NEW
        private Integer remainingQuantity; // NEW
    }
}
