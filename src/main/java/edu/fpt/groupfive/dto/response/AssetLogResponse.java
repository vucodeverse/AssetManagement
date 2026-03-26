package edu.fpt.groupfive.dto.response;

import edu.fpt.groupfive.common.AssetActionType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssetLogResponse {

    private Integer assetLogId;
    private Integer assetId;
    private AssetActionType actionType;

    private String fromDepartmentName;
    private String toDepartmentName;

    private LocalDateTime actionDate;

    private String oldStatus;
    private String newStatus;

    private String note;
}
