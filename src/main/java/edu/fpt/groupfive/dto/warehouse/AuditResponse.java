package edu.fpt.groupfive.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditResponse {
    private Integer id;
    private Integer warehouseId;
    private Integer zoneId;
    private String status;
    private Integer auditorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String note;
}
