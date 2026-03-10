package edu.fpt.groupfive.dto.warehouse.request;

import lombok.Data;

@Data
public class TicketDetailRequestDto {
    private Integer assetTypeId;
    private Integer quantity;
    private String note;
}
