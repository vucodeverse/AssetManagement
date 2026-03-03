package edu.fpt.groupfive.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class OrderDetail extends AbstractEntity<Integer> {
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal taxRate;
    private BigDecimal discountRate;
    private String orderDetailNote;
    private Integer assetTypeId;
    private Integer quotationDetailId;
    private LocalDate expectedDeliveryDate;
}
