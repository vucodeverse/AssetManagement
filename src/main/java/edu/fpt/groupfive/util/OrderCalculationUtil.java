package edu.fpt.groupfive.util;

import edu.fpt.groupfive.dto.request.PurchaseOrderCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseOrderDetailCreateRequest;
import edu.fpt.groupfive.dto.request.QuotationDetailCreateRequest;
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

    // tính toán từng dòng  1
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

    // tính toán lại giá tiền khi tạo purchase order
    public void recalculateTotal(PurchaseOrderCreateRequest request) {

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderDetailCreateRequest line : request.getPurchaseOrderDetailCreateRequests()) {
            if (line.getQuantity() == null) continue;
            total = total.add(calculateLineTotal(
                    new BigDecimal(line.getQuantity()),
                    line.getPrice(),
                    line.getDiscountRate(),
                    line.getTaxRate()));
        }
        request.setTotalAmount(total.setScale(SCALE, RoundingMode.HALF_UP));
    }

    // tính toán giá cho quotation
    public BigDecimal calculateTotal(QuotationCreateRequest request) {
        BigDecimal total = BigDecimal.ZERO;
        for (QuotationDetailCreateRequest qd : request.getQuotationDetailCreateRequests()) {
            total = total.add(calculateLineTotal(
                    qd.getQuantity() != null ? BigDecimal.valueOf(qd.getQuantity()) : null,
                    qd.getPrice(),
                    qd.getDiscountRate(),
                    qd.getTaxRate()));
        }
        return total;
    }

    // tính toán cho từng quotation detail và total của quotation
    public BigDecimal[] calculateQuotationPrice(List<QuotationDetail> details) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        // duyệt từng quotation detail
        for (QuotationDetail qd : details) {

            BigDecimal discountRate = qd.getDiscountRate() != null ? qd.getDiscountRate() : BigDecimal.ZERO;

            BigDecimal taxRate = qd.getTaxRate() != null ? qd.getTaxRate() : BigDecimal.ZERO;

            BigDecimal qty = qd.getQuantity() != null ? BigDecimal.valueOf(qd.getQuantity()) : BigDecimal.ZERO;

            BigDecimal price = qd.getPrice() != null  ? qd.getPrice() : BigDecimal.ZERO;

            // tính tổng riêng từng detail
            BigDecimal lineTotal = qty.multiply(price);

            // tính discount cho từng detail làm tròn đế chữ số tp thứ 2
            BigDecimal lineDiscount = lineTotal.multiply(discountRate).divide(HUNDRED, SCALE, RoundingMode.HALF_UP);

            // tính số tiền sẽ phải chiu thuế
            BigDecimal taxableAmount = lineTotal.subtract(lineDiscount);

            // tính t huế
            BigDecimal tax = taxableAmount.multiply(taxRate).divide(HUNDRED, SCALE, RoundingMode.HALF_UP);

            // tính tổng
            subtotal = subtotal.add(lineTotal);
            totalDiscount = totalDiscount.add(lineDiscount);
            totalTax = totalTax.add(tax);
        }

        BigDecimal grandTotal = subtotal.subtract(totalDiscount).add(totalTax);
        return new BigDecimal[] { subtotal, totalDiscount, totalTax, grandTotal };
    }

    public BigDecimal[] calculatePoDetail(List<OrderDetail> details) {
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
