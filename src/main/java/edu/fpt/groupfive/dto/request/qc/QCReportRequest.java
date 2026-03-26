package edu.fpt.groupfive.dto.request.qc;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class QCReportRequest {
    private Integer assetId;
    private String status;
    private Integer inspectedBy;
    private String note;
}

