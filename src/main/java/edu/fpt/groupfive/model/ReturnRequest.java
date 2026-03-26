package edu.fpt.groupfive.model;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ReturnRequest {
    private Integer requestId;

    private Integer requesterId;

    private Integer requestedDepartmentId;

    private LocalDateTime requestDate;

    private String status;

    private String requestReason;

    private LocalDateTime createdAt;

    private LocalDateTime updateAt;

    // Attribute bổ trợ
    private String fullName;
    private String departmentName;
}
