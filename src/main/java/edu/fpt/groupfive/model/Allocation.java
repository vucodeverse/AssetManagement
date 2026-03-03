package edu.fpt.groupfive.model;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Allocation {
    private Integer allocationId;

    // Liên kết ngược lại yêu cầu của Trưởng phòng
    private Integer requestId;

    // ID của Asset Manager người thực hiện
    private Integer allocatedByUserId;

    // Đến phòng ban nào
    private Integer allocatedToDepartmentId;

    private LocalDateTime allocationDate;

    private String status;
    private String note;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
