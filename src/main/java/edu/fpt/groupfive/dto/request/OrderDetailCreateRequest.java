package edu.fpt.groupfive.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class OrderDetailCreateRequest {
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal taxRate;
    private Integer assetTypeId;
    private Integer quotationDetailId;
    private String orderDetailNote;
    private BigDecimal discountRate;
    private LocalDate expectedDeliveryDate;
}
