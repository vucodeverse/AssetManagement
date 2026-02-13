package edu.fpt.groupfive.model;

import edu.fpt.groupfive.common.OrderStatus;
import edu.fpt.groupfive.common.Request;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class Order extends AbstractEntity<Integer>{
    private BigDecimal totalAmount;
    private String purchaseOrderNote;
    private OrderStatus orderStatus;
    private Integer supplierId;
    private String orderNote;
    private Integer quotationId;
    private Integer approvedBy;
    private Integer updateBy;
    private List<OrderDetail> orderDetails;

}
