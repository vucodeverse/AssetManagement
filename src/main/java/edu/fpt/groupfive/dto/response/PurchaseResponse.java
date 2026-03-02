package edu.fpt.groupfive.dto.response;

import edu.fpt.groupfive.common.Request;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseResponse {
    private Integer purchaseId;
    private Request status;
    private String creatorName;
    private LocalDate neededByDate;
    private String priority;
    private LocalDate createdAt;
    private List<PurchaseDetailResponse> purchaseDetails;
    private Integer quotationCount;
    private String rejectReason;
}
