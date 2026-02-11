package edu.fpt.groupfive.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class QuotationDetailResponse {

    private String assetTypeName;
    private Integer quantity;
    private Integer warrantyMonths;
    private BigDecimal price;
    private String quotationDetailNote;

}
