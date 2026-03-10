package edu.fpt.groupfive.dto.warehouse.response;

import edu.fpt.groupfive.model.warehouse.InventoryTicket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TicketMappingResponseDto {
    private InventoryTicket ticket;

    // List of ticket details containing requested vs mapped quantity
    private List<TicketDetailMappingDto> details;

    // List of currently mapped physical assets
    private List<TicketMappedAssetDto> mappedAssets;

    // Progress helpers
    private int totalRequested;
    private int totalMapped;
    private int progressPercentage;
}
