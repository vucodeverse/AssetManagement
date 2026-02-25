package edu.fpt.groupfive.util;

import edu.fpt.groupfive.dto.request.OrderCreateRequest;
import edu.fpt.groupfive.dto.request.OrderDetailCreateRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class OrderCalculationUtil {

    public void recalculateTotal(OrderCreateRequest request) {
        if (request == null || request.getOrderDetailCreateRequests() == null || request.getOrderDetailCreateRequests().isEmpty()) {
            if (request != null) request.setTotalAmount(BigDecimal.ZERO);
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (OrderDetailCreateRequest line : request.getOrderDetailCreateRequests()) {
            if (line.getQuantity() != null && line.getPrice() != null) {
                BigDecimal qty = new BigDecimal(line.getQuantity());
                BigDecimal tax = line.getTaxRate() != null ? line.getTaxRate() : BigDecimal.ZERO;
                BigDecimal disc = line.getDiscountRate() != null ? line.getDiscountRate() : BigDecimal.ZERO;

                // factor = 1 + tax/100 - disc/100
                BigDecimal factor = BigDecimal.ONE
                        .add(tax.divide(new BigDecimal(100), 4, RoundingMode.HALF_UP))
                        .subtract(disc.divide(new BigDecimal(100), 4, RoundingMode.HALF_UP));

                total = total.add(qty.multiply(line.getPrice()).multiply(factor));
            }
        }
        request.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
    }
}
