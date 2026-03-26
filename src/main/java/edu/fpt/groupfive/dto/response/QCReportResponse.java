package edu.fpt.groupfive.dto.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Setter
@Getter

public class QCReportResponse {
    private Integer reportId;
    private Integer assetId;
    private String status;
    private Integer inspectedBy;
    private String inspectorName;
    private LocalDateTime qcDate;
    private String note;
}