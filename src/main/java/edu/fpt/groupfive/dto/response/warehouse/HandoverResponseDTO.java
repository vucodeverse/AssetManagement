package edu.fpt.groupfive.dto.response.warehouse;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class HandoverResponseDTO {
    private Integer handoverId;
    private String fromDepartmentName;
    private String toDepartmentName;
    private String createdBy;
    private LocalDateTime createdAt;
    private String status; // PENDING, COMPLETE
}
