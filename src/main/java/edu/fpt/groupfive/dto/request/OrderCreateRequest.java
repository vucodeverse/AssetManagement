package edu.fpt.groupfive.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateRequest {
    private Integer quotationId;
    private Integer supplierId;
    private String orderNote;
    private BigDecimal totalAmount;

    @Builder.Default
    private List<OrderDetailCreateRequest> orderDetailCreateRequests = new ArrayList<>();
}
