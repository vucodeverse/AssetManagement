package edu.fpt.groupfive.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AllocationRequestDetail {
    private Integer requestDetailId;
    private Integer requestId;
    private Integer assetTypeId;
    private Integer requestedQuantity;
//    private String issuedCondition;
    private String note;
}
