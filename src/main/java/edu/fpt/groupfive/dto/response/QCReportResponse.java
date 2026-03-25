package edu.fpt.groupfive.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QCReportResponse {
    private int reportId;
    private int assetId;
    private String status;
    private int inspectedBy;
    private String inspectorName;
    private LocalDateTime qcDate;
    private String note;
    private String attachment;

}