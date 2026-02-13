package edu.fpt.groupfive.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateRequest {
    private Integer quotationId;
    private Integer supplierId;
    private String orderNote;
    private BigDecimal totalAmout;

    @Builder.Default
    private List<OrderDetailCreateRequest> orderDetailCreateRequests = new ArrayList<>();
}
