package edu.fpt.groupfive.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderGroupResponse {
    private Integer purchaseRequestId;
    private String purchaseRequestNote;
    private List<PurchaseOrderResponse> orders;
}
