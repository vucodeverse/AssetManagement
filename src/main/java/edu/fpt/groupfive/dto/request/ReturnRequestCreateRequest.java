package edu.fpt.groupfive.dto.request;

import edu.fpt.groupfive.model.ReturnRequestDetail;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ReturnRequestCreateRequest {
    private Integer requestId;
    private Integer requesterId;
    private Integer requestedDepartmentId;
    private String status;
    private String requestReason;
    private List<ReturnRequestDetailRequest> details;
}
