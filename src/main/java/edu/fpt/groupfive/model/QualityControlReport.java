package edu.fpt.groupfive.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class QualityControlReport {
    private int reportId;
    private int assetId;
    private String status;
    private int inspectedBy;
    private LocalDateTime createdDate;
    private String note;

}
