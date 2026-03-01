package edu.fpt.groupfive.util;

import edu.fpt.groupfive.dto.request.OrderCreateRequest;
import edu.fpt.groupfive.dto.request.OrderDetailCreateRequest;
import edu.fpt.groupfive.dto.request.QuotationCreateDetailRequest;
import edu.fpt.groupfive.dto.request.QuotationCreateRequest;
import edu.fpt.groupfive.model.OrderDetail;
import edu.fpt.groupfive.model.QuotationDetail;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class OrderCalculationUtil {

    private static final int SCALE = 2;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    // ===== Core: tính tiền 1 dòng (discount trước thuế) =====
    public BigDecimal calculateLineTotal(BigDecimal qty, BigDecimal price,
            BigDecimal discountRate, BigDecimal taxRate) {
        if (qty == null || price == null)
            return BigDecimal.ZERO;

        discountRate = discountRate != null ? discountRate : BigDecimal.ZERO;
        taxRate = taxRate != null ? taxRate : BigDecimal.ZERO;

        BigDecimal lineSubtotal = qty.multiply(price);
        BigDecimal discount = lineSubtotal.multiply(discountRate)
                .divide(HUNDRED, SCALE, RoundingMode.HALF_UP);
        BigDecimal taxableAmount = lineSubtotal.subtract(discount);
        BigDecimal tax = taxableAmount.multiply(taxRate)
                .divide(HUNDRED, SCALE, RoundingMode.HALF_UP);

        return taxableAmount.add(tax);
    }

    // ===== Order: tính lại tổng cho PO form =====
    public void recalculateTotal(OrderCreateRequest request) {
        if (request == null || request.getOrderDetailCreateRequests() == null
                || request.getOrderDetailCreateRequests().isEmpty()) {
            if (request != null)
                request.setTotalAmount(BigDecimal.ZERO);
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (OrderDetailCreateRequest line : request.getOrderDetailCreateRequests()) {
            total = total.add(calculateLineTotal(
                    line.getQuantity() != null ? new BigDecimal(line.getQuantity()) : null,
                    line.getPrice(),
                    line.getDiscountRate(),
                    line.getTaxRate()));
        }
        request.setTotalAmount(total.setScale(SCALE, RoundingMode.HALF_UP));
    }

    // ===== Quotation: tính total từ QuotationCreateRequest =====
    public BigDecimal calculateTotal(QuotationCreateRequest request) {
        BigDecimal total = BigDecimal.ZERO;
        for (QuotationCreateDetailRequest qd : request.getQuotationCreateDetailRequestList()) {
            total = total.add(calculateLineTotal(
                    qd.getQuantity() != null ? BigDecimal.valueOf(qd.getQuantity()) : null,
                    qd.getPrice(),
                    qd.getDiscountRate(),
                    qd.getTaxRate()));
        }
        return total;
    }

    // ===== Quotation Detail: tính breakdown đầy đủ (dùng cho quotation-detail
    // page) =====
    // Trả về [subtotal, totalDiscount, totalTax, grandTotal]
    public BigDecimal[] calculateBreakdown(List<QuotationDetail> details) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (QuotationDetail qd : details) {
            BigDecimal discountRate = qd.getDiscountRate() != null ? qd.getDiscountRate() : BigDecimal.ZERO;
            BigDecimal taxRate = qd.getTaxRate() != null ? qd.getTaxRate() : BigDecimal.ZERO;
            BigDecimal qty = qd.getQuantity() != null ? BigDecimal.valueOf(qd.getQuantity()) : BigDecimal.ZERO;
            BigDecimal price = qd.getPrice() != null ? qd.getPrice() : BigDecimal.ZERO;

            BigDecimal lineTotal = qty.multiply(price);
            BigDecimal lineDiscount = lineTotal.multiply(discountRate)
                    .divide(HUNDRED, SCALE, RoundingMode.HALF_UP);
            BigDecimal taxableAmount = lineTotal.subtract(lineDiscount);
            BigDecimal tax = taxableAmount.multiply(taxRate)
                    .divide(HUNDRED, SCALE, RoundingMode.HALF_UP);

            subtotal = subtotal.add(lineTotal);
            totalDiscount = totalDiscount.add(lineDiscount);
            totalTax = totalTax.add(tax);
        }

        BigDecimal grandTotal = subtotal.subtract(totalDiscount).add(totalTax);
        return new BigDecimal[] { subtotal, totalDiscount, totalTax, grandTotal };
    }

    public BigDecimal[] calculateBreakdownForOrder(List<OrderDetail> details) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (OrderDetail od : details) {
            BigDecimal discountRate = od.getDiscountRate() != null ? od.getDiscountRate() : BigDecimal.ZERO;
            BigDecimal taxRate = od.getTaxRate() != null ? od.getTaxRate() : BigDecimal.ZERO;
            BigDecimal qty = od.getQuantity() != null ? BigDecimal.valueOf(od.getQuantity()) : BigDecimal.ZERO;
            BigDecimal price = od.getPrice() != null ? od.getPrice() : BigDecimal.ZERO;

            BigDecimal lineTotal = qty.multiply(price);
            BigDecimal lineDiscount = lineTotal.multiply(discountRate)
                    .divide(HUNDRED, SCALE, RoundingMode.HALF_UP);
            BigDecimal taxableAmount = lineTotal.subtract(lineDiscount);
            BigDecimal tax = taxableAmount.multiply(taxRate)
                    .divide(HUNDRED, SCALE, RoundingMode.HALF_UP);

            subtotal = subtotal.add(lineTotal);
            totalDiscount = totalDiscount.add(lineDiscount);
            totalTax = totalTax.add(tax);
        }

        BigDecimal grandTotal = subtotal.subtract(totalDiscount).add(totalTax);
        return new BigDecimal[] { subtotal, totalDiscount, totalTax, grandTotal };
    }
}
