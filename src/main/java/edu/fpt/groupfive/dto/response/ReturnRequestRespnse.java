package edu.fpt.groupfive.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequestRespnse {
    private Integer requestId;
    private Integer requesterId;
    private Integer requestedDepartmentId;
    private String status;
    private String requestReason;

    private LocalDateTime createdAt;
    private List<ReturnRequestDetailResponse> details;

    // Attribute bổ trợ
    private String fullName;
    private String departmentName;
}
