package edu.fpt.groupfive.dto.warehouse.request;

import lombok.Data;

@Data
public class InboundPORequest {
    private Integer purchaseOrderId;
    private Integer assetTypeId;
    private Integer quantity;
    private String note;
}
