package edu.fpt.groupfive.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderDetailCreateRequest {
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal taxRate;
    private String assetTypeName;
    private Integer quotationDetailId;
    private String orderDetailNote;
    private BigDecimal discountRate;
}
