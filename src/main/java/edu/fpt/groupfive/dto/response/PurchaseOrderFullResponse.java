package edu.fpt.groupfive.dto.response;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderFullResponse extends PurchaseOrderResponse {
    // orderNote corresponds to note in parent
    // items
    private List<PurchaseOrderDetailResponse> items;

    // Totals
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal totalTax;
    // grandTotal should correspond to totalAmount in parent
}
