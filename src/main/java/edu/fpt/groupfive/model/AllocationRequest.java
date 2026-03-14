package edu.fpt.groupfive.model;

import lombok.*;

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
    // Hỗ trợ khi hiển thị tên chứ ko thuộc database feild
    private String userName;
    private String requestedDepartmentName;
    private Integer assetManagerName;
}
