package edu.fpt.groupfive.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OrderDetail {
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal taxRate;
    private BigDecimal discountRate;
    private String orderDetailNote;
    private Integer assetTypeId;
    private Integer quotationDetailId;
    private LocalDate expectedDeliveryDate;
    private Integer orderId;
}
