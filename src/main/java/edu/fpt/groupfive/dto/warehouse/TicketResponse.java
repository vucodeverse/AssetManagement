package edu.fpt.groupfive.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private Integer id;
    private Integer warehouseId;
    private String ticketType;
    private String ticketRef;
    private String status;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String note;
    private List<TicketDetailResponse> details;
}
