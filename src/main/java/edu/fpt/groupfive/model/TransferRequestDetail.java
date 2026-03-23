package edu.fpt.groupfive.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TransferRequestDetail {
    private int transferDetailId;
    private int transferId;
    private Integer allocationRequestDetailId;
    private int assetId;
    private String conditionFromSender;
    private String note;
}