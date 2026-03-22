package edu.fpt.groupfive.dto.response.warehouse;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class HandoverDetailResponseDTO {
    private Integer handoverId;
    private String fromDepartmentName;
    private String toDepartmentName;
    private String status;
    private List<HandoverItemDTO> items;

    @Data
    @Builder
    public static class HandoverItemDTO {
        private Integer assetId;
        private String assetCode;
        private String assetTypeName;
        private boolean isScanned;
    }
}
