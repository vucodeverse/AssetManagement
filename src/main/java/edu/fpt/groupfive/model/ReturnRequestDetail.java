package edu.fpt.groupfive.model;


import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ReturnRequestDetail {
    private Integer requestDetailId;
    private Integer requestId;
    private Integer assetId;
    private String note;
}
