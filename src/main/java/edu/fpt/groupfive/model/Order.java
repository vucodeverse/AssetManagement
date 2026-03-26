package edu.fpt.groupfive.model;

import edu.fpt.groupfive.common.PurchaseProcessStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class Order extends AbstractEntity<Integer>{

    private BigDecimal totalAmount;
    private PurchaseProcessStatus orderStatus;
    private String orderNote;
    private Integer purchaseId;
    private Integer supplierId;
    private Integer quotationId;
    private Integer approvedBy;
    private Integer updatedBy;
    private List<OrderDetail> orderDetails;
}
