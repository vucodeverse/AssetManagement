package edu.fpt.groupfive.dto.warehouse.request;

import lombok.Data;

@Data
public class InboundReturnRequest {
    private Integer returnRequestId;
    private Integer assetId;
    private String conditionStatus; // READY_TO_USE or MAINTENANCE
    private String note;
}
