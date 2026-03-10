package edu.fpt.groupfive.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AllocationRequest {
    private Integer requestId;

    private Integer requesterId;

    private Integer requestedDepartmentId;

    private LocalDateTime requestDate;

    private String requesterName;

    private String requestedDepartmentName;

    private String status;

    private String requestReason;

    private String priority;

    private LocalDate neededByDate;

    // Phân quyền Asset Manager duyệt
    private Integer assetManagerApprovedByUserId;

    private LocalDateTime assetManagerApprovedDate;

    private String rejectReason;

    private LocalDateTime createdAt;

    private LocalDateTime updateAt;
}
