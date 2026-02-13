package edu.fpt.groupfive.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class OrderCreateRequest {
    private Integer quotationId;
    private String supplierName;
    private String orderNote;
    private BigDecimal totalAmout;

    @Builder.Default
    private List<OrderDetailCreateRequest> orderDetailCreateRequests = new ArrayList<>();
}
