package edu.fpt.groupfive.dto.response.warehouse;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class POResponseDTO {
    private Integer purchaseOrderId;
    private String supplierName;
    private Long totalAmount;
    private LocalDateTime createdAt;
    private String status;
}
