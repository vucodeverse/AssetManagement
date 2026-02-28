package edu.fpt.groupfive.util;

import edu.fpt.groupfive.dto.request.OrderCreateRequest;
import edu.fpt.groupfive.dto.request.OrderDetailCreateRequest;
import edu.fpt.groupfive.dto.request.QuotationCreateDetailRequest;
import edu.fpt.groupfive.dto.request.QuotationCreateRequest;
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

                BigDecimal factor = BigDecimal.ONE
                        .add(tax.divide(new BigDecimal(100), 4, RoundingMode.HALF_UP))
                        .subtract(disc.divide(new BigDecimal(100), 4, RoundingMode.HALF_UP));

                total = total.add(qty.multiply(line.getPrice()).multiply(factor));
            }
        }
        request.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
    }

    // tính total
    public BigDecimal calculateTotal(QuotationCreateRequest request) {
        BigDecimal total = BigDecimal.ZERO;

        // duyệt từng detail 1
        for (QuotationCreateDetailRequest quotationCreateDetailRequest : request
                .getQuotationCreateDetailRequestList()) {
            if (quotationCreateDetailRequest.getPrice() != null && quotationCreateDetailRequest.getQuantity() != null) {

                // parse
                BigDecimal qty = BigDecimal.valueOf(quotationCreateDetailRequest.getQuantity());
                BigDecimal price = quotationCreateDetailRequest.getPrice();

                // tính total của từng line
                BigDecimal lineSubtotal = qty.multiply(price);

                BigDecimal discountRate = quotationCreateDetailRequest.getDiscountRate() != null
                        ? quotationCreateDetailRequest.getDiscountRate()
                        : BigDecimal.ZERO;

                // tính discount cho từng line
                BigDecimal discount = lineSubtotal.multiply(discountRate).divide(BigDecimal.valueOf(100), 2,
                        RoundingMode.HALF_UP);

                // tính ra số tiền phải chịu thuế
                BigDecimal taxableAmount = lineSubtotal.subtract(discount);

                // lấy ra thuế
                BigDecimal taxRate = quotationCreateDetailRequest.getTaxRate() != null
                        ? quotationCreateDetailRequest.getTaxRate()
                        : BigDecimal.ZERO;

                // tnh thuế
                BigDecimal tax = taxableAmount.multiply(taxRate).divide(BigDecimal.valueOf(100), 2,
                        java.math.RoundingMode.HALF_UP);

                total = total.add(taxableAmount.add(tax));
            }
        }
        return total;
    }

}
