package edu.fpt.groupfive.dto.request;

import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationCreateRequest {
    private Integer purchaseRequestId;
    private String quotationNote;
    private Integer supplierId;
    private List<QuotationCreateDetailRequest> quotationCreateDetailRequestList = new ArrayList<>();
}
