package edu.fpt.groupfive.model.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAudit {
    private Integer id;
    private Integer warehouseId;
    private Integer zoneId;
    private String status; // IN_PROGRESS, COMPLETED
    private Integer auditorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String note;
}
