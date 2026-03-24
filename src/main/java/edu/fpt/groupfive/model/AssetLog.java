package edu.fpt.groupfive.model;

import edu.fpt.groupfive.common.AssetActionType;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssetLog {
    private Integer assetLogId;
    private Integer assetId;
    private AssetActionType actionType;
    private Integer fromDepartmentId;
    private Integer toDepartmentId;
    private LocalDateTime actionDate;
    private String oldStatus;
    private String newStatus;
    private Integer relatedAllocationId;
    private Integer relatedTransferId;
    private Integer relatedReturnId;
    private String note;
    private Integer createdBy;
}
