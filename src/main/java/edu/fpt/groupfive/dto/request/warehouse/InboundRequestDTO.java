package edu.fpt.groupfive.dto.request.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InboundRequestDTO {
    private Integer poId;
    private String note;
    private List<InboundItemRequestDTO> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InboundItemRequestDTO {
        private Integer poDetailId;
        private Integer quantityToReceive;
    }
}
