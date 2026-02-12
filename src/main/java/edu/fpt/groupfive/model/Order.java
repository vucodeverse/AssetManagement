package edu.fpt.groupfive.model;

import edu.fpt.groupfive.common.Request;

import java.math.BigDecimal;
import java.util.List;

public class Order extends AbstractEntity<Integer>{
    private BigDecimal totalAmount;
    private String purchaseOrderNote;
    private Request orderStatus;
    private Integer supplierId;
    private String orderNote;
    private Integer quotationId;
    private Integer approvedBy;
    private Integer updateBy;
    private List<OrderDetail> orderDetails;

}
