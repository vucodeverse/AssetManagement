package edu.fpt.groupfive.dto.warehouse.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketDetailResponseDto {
    private Integer id;
    private Integer ticketId;
    private Integer assetTypeId;
    private String assetTypeName;
    private Integer quantity;
    private String note;
}
