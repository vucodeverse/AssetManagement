package edu.fpt.groupfive.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditCreateRequest {
    private Integer warehouseId;
    private Integer zoneId;
    private Integer auditorId;
    private String note;
}
