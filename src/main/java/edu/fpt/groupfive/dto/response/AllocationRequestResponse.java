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
@Builder
public class AllocationRequestResponse {
    private Integer requestId;
    private Integer requesterId;
    private Integer requestedDepartmentId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate neededByDate;

    private String priority;
    private String requestReason;
    private String status;

    private Integer amApprovedBy;
    private LocalDateTime amApprovedAt;
    private String reasonReject;

    private LocalDateTime createdAt;

    private List<AllocationRequestDetailResponse> details;

    private String userName;
    private String requestedDepartmentName;
    private String amApprovedName;
}
