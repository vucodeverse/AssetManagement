package edu.fpt.groupfive.dto.response.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QCReportRequestDTO {
    private Integer handoverId;
    private List<QCItemDTO> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QCItemDTO {
        private Integer assetId;
        private String assetCode;
        private String assetTypeName;
        private String condition;
        private String note;
    }
}
