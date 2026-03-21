package edu.fpt.groupfive.model;


import edu.fpt.groupfive.common.Status;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssetHandover {
    private Integer handoverId;
    private String handoverType;
    private Integer allocationRequestId;
    private Integer returnRequestId;
    private Integer fromDepartmentId;
    private Integer toDepartmentId;
    private Status status;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
