package edu.fpt.groupfive.dto.response;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AllocationRequestDetailResponse {
    private Integer assetTypeId;
    private Integer requestedQuantity;
    private String note;
}
