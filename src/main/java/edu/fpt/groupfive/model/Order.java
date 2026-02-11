package edu.fpt.groupfive.model;

import edu.fpt.groupfive.common.Request;

import java.math.BigDecimal;

public class Order extends AbstractEntity<Integer>{
    private BigDecimal totalAmount;
    private String purchaseOrderNote;
    private Request orderStatus;
}
