package edu.fpt.groupfive.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AllocationRequestDetail {
    private Integer requestDetailId;
    private Integer requestId;
    private Integer assetTypeId;
    private Integer requestedQuantity;
    private String note;
}
