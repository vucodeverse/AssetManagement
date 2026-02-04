package edu.fpt.groupfive.dto.request;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Builder
public class PurchaseCreateRequest {

    private String note;
    private Date neededByDate;
    private String reason;
    private String priority;
    private List<PurchaseDetailCreateRequest> purchaseDetailCreateRequests;
}
