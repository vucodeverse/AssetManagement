package edu.fpt.groupfive.dto.response;

import edu.fpt.groupfive.common.Request;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseRequestResponse {
    private Integer purchaseId;
    private Request status;
    private String creatorName;
    private LocalDate neededByDate;
    private String priority;
    private LocalDateTime createdAt;
    private List<PurchaseRequestDetailResponse> purchaseDetails;
    private Integer quotationCount;
    private String rejectReason;
    private BigDecimal totalAmount;
}
