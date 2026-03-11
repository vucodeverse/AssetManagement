package edu.fpt.groupfive.dto.response;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AllocationRequestResponse {
    private Integer requestId;
    private Integer requesterId;
    private Integer requestedDepartmentId;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate neededByDate;
    //fix sau
    private String requesterName;
    private String requestedDepartmentName;
    private String priority;
    private String requestReason;
    private String status;

    private Integer amApprovedBy;
    private String amApprovedName;
    private LocalDateTime amApprovedAt;
    private String reasonReject;

    private LocalDateTime createdAt;

    private List<AllocationRequestDetailResponse> details;
}
