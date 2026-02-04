package edu.fpt.groupfive.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class PurchaseDetailCreateRequest {
    private Integer quantity;
    private String specification_requirement;
    private String note;
    private Integer assetTypeId;
}
