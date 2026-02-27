package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.OrderStatus;
import edu.fpt.groupfive.common.QuotationStatus;
import edu.fpt.groupfive.dao.*;
import edu.fpt.groupfive.dto.request.OrderCreateRequest;
import edu.fpt.groupfive.dto.request.OrderDetailCreateRequest;
import edu.fpt.groupfive.dto.request.OrderSearchCriteria;
import edu.fpt.groupfive.dto.response.OrderDetailResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderDetailResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderGroupResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;
import edu.fpt.groupfive.mapper.OrderDetailMapper;
import edu.fpt.groupfive.mapper.OrderMapper;
import edu.fpt.groupfive.model.*;
import edu.fpt.groupfive.service.OrderService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
@Slf4j(topic = "ORDER-SERVICE")
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final QuotationDAO quotationDAO;
    private final QuotationDetailDAO quotationDetailDAO;
    private final OrderDetailMapper orderDetailMapper;
    private final OrderMapper orderMapper;
    private final OrderDAO orderDAO;
    private final OrderDetailDAO orderDetailDAO;
    private final SupplierDAO supplierDAO;
    private final AssetTypeDAO assetTypeDAO;
    private final PurchaseDetailDAO purchaseDetailDAO;

    @Override
    public OrderCreateRequest checkFormCreateOrder(Integer quotationId) {
        Quotation quotation = quotationDAO.findById(quotationId)
                .orElseThrow(() -> new InvalidDataException("Quotation không tồn tại: " + quotationId));

        List<QuotationDetail> quotationDetails = quotationDetailDAO.findByQuotationId(quotationId);

        // Chúng ta cần map Asset Type Name để hiển thị lên form, nhưng cần Asset Type
        // ID để lưu PO sau này.
        // Lấy map Asset Type Name từ AssetTypeDAO
        Map<Integer, String> assetTypeNames = assetTypeDAO.findAll().stream()
                .collect(Collectors.toMap(AssetType::getTypeId, AssetType::getTypeName));

        List<OrderDetailCreateRequest> orderDetailCreateRequests = quotationDetails.stream()
                .map(qd -> OrderDetailCreateRequest.builder()
                        .quotationDetailId(qd.getId())
                        .discountRate(qd.getDiscountRate())
                        .taxRate(qd.getTaxRate())
                        .price(qd.getPrice())
                        .assetTypeId(qd.getAssetTypeId())
                        .assetTypeName(assetTypeNames.getOrDefault(qd.getAssetTypeId(), "N/A"))
                        .quantity(qd.getQuantity())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));

        return OrderCreateRequest.builder()
                .totalAmount(quotation.getTotalAmount())
                .supplierId(quotation.getSupplierId())
                .quotationId(quotationId)
                .orderDetailCreateRequests(orderDetailCreateRequests)
                .build();
    }

    // ── Tạo Purchase Order ─────────────────────────────────────────────────

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer createOrder(Integer quotationId, OrderCreateRequest req) {

        // 1. Validate quotation tồn tại
        Quotation quotation = quotationDAO.findById(quotationId)
                .orElseThrow(() -> new InvalidDataException("Quotation không tồn tại: " + quotationId));

        // 2. Load toàn bộ QuotationDetail từ DB → dùng Map để tra cứu nhanh O(1)
        List<QuotationDetail> quotationDetails = quotationDetailDAO.findByQuotationId(quotationId);
        Map<Integer, QuotationDetail> quotationDetailMap = quotationDetails.stream()
                .collect(Collectors.toMap(QuotationDetail::getId, qd -> qd));

        // 3. Validate danh sách lines từ form
        List<OrderDetailCreateRequest> lines = req.getOrderDetailCreateRequests();
        if (lines == null || lines.isEmpty()) {
            throw new InvalidDataException("Cần ít nhất 1 dòng để tạo PO");
        }

        // Lọc bỏ dòng có quantity null hoặc <= 0
        List<OrderDetailCreateRequest> validLines = lines.stream()
                .filter(o -> o.getQuantity() != null && o.getQuantity() > 0)
                .collect(Collectors.toList());

        if (validLines.isEmpty()) {
            throw new InvalidDataException("Không có dòng hợp lệ nào để tạo PO");
        }

        // 4. Kiểm tra trùng quotationDetailId trong cùng 1 request
        Set<Integer> seen = new HashSet<>();
        for (OrderDetailCreateRequest line : validLines) {
            if (!seen.add(line.getQuotationDetailId())) {
                throw new InvalidDataException("Duplicate quotation detail id: " + line.getQuotationDetailId());
            }
        }

        // 5. Kiểm tra số lượng dựa trên PR (Purchase Request)
        // Lấy danh sách Purchase Detail IDs từ Quotation Details
        List<Integer> prDetailIds = quotationDetails.stream()
                .map(QuotationDetail::getPurchaseDetailId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Lấy số lượng đã đặt của từng dòng PR (tổng hợp từ TẤT CẢ các PO trước đó)
        Map<Integer, Integer> orderedQtyByPrDetail = orderDAO.getOrderedQtyByPurchaseDetail(prDetailIds);

        // Lấy thông tin PR Detail gốc để biết số lượng yêu cầu ban đầu
        List<PurchaseDetail> prDetails = purchaseDetailDAO.findByPurchaseRequestId(quotation.getPurchaseId());
        Map<Integer, PurchaseDetail> prDetailMap = prDetails.stream()
                .collect(Collectors.toMap(PurchaseDetail::getId, pd -> pd));

        // 6. Tạo Order header
        Order order = new Order();
        order.setQuotationId(quotationId);
        order.setPurchaseRequestId(quotation.getPurchaseId());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderNote(req.getOrderNote());
        order.setSupplierId(quotation.getSupplierId());
        order.setTotalAmount(req.getTotalAmount());

        Integer orderId = orderDAO.insert(order);
        if (orderId == null || orderId <= 0) {
            throw new InvalidDataException("Tạo purchase order thất bại");
        }

        // 7. Tạo từng OrderDetail
        for (OrderDetailCreateRequest line : validLines) {
            QuotationDetail qd = quotationDetailMap.get(line.getQuotationDetailId());
            if (qd == null) {
                throw new InvalidDataException("Quotation detail không tồn tại: " + line.getQuotationDetailId());
            }

            PurchaseDetail prDetail = prDetailMap.get(qd.getPurchaseDetailId());
            if (prDetail == null) {
                throw new InvalidDataException("Không tìm thấy thông tin Purchase Request gốc cho dòng này.");
            }

            int alreadyOrdered = orderedQtyByPrDetail.getOrDefault(qd.getPurchaseDetailId(), 0);
            int remainingInPr = prDetail.getQuantity() - alreadyOrdered;

            if (line.getQuantity() > remainingInPr) {
                throw new InvalidDataException(String.format(
                        "Số lượng đặt hàng (%d) vượt quá số lượng còn lại trong PR (%d). Đã đặt trước đó: %d/%d.",
                        line.getQuantity(), remainingInPr, alreadyOrdered, prDetail.getQuantity()));
            }

            // Dùng mapper để map các field chung (quantity, note, quotationDetailId)
            OrderDetail orderDetail = orderDetailMapper.toOrderDetail(line);

            // Override các field lấy từ DB (không tin form) để tránh tampering
            orderDetail.setPrice(qd.getPrice());
            orderDetail.setTaxRate(qd.getTaxRate());
            orderDetail.setDiscountRate(qd.getDiscountRate());
            orderDetail.setAssetTypeId(qd.getAssetTypeId());

            orderDetailDAO.insetOrderDetail(orderDetail, orderId);
        }

        // 8. Update quotation status to APPROVED
        quotationDAO.updateStatusReject(quotationId, QuotationStatus.APPROVED, null);

        return orderId;
    }

    @Override
    public List<PurchaseOrderGroupResponse> getOrdersGroupedByPR(OrderSearchCriteria criteria) {
        parseAmountRange(criteria);

        List<Object[]> results = orderDAO.searchAndFilter(criteria);

        LinkedHashMap<Integer, PurchaseOrderGroupResponse> groupMap = new LinkedHashMap<>();

        for (Object[] row : results) {
            Order order = (Order) row[0];
            String supplierName = (String) row[1];
            int purchaseRequestId = order.getPurchaseRequestId();

            PurchaseOrderResponse poResponse = orderMapper.toPurchaseOrderResponse(order);
            poResponse.setSupplierName(supplierName);

            groupMap.computeIfAbsent(purchaseRequestId, prId -> PurchaseOrderGroupResponse.builder()
                    .purchaseRequestId(prId)
                    .orders(new ArrayList<>())
                    .build()).getOrders().add(poResponse);
        }

        return new ArrayList<>(groupMap.values());
    }

    @Override
    public PurchaseOrderDetailResponse getOrderDetail(Integer orderId) {
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new InvalidDataException("Purchase Order không tồn tại: " + orderId));

        String supplierName = supplierDAO.findById(order.getSupplierId())
                .map(s -> s.getSupplierName())
                .orElse("N/A");

        List<OrderDetail> detailModels = orderDetailDAO.findByOrderId(orderId);

        List<OrderDetailResponse> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        for (OrderDetail detail : detailModels) {
            String assetTypeName = assetTypeDAO.findById(detail.getAssetTypeId());

            // Mapping via MapStruct
            OrderDetailResponse itemDto = orderDetailMapper.toOrderDetailResponse(detail);
            // Enrichment
            itemDto.setAssetTypeName(assetTypeName);
            items.add(itemDto);

            // Calculation logic
            BigDecimal lineQty = BigDecimal.valueOf(detail.getQuantity());
            BigDecimal linePrice = detail.getPrice();
            BigDecimal lineSubtotal = lineQty.multiply(linePrice);

            BigDecimal discountRate = detail.getDiscountRate() != null ? detail.getDiscountRate() : BigDecimal.ZERO;
            BigDecimal lineDiscount = lineSubtotal.multiply(discountRate).divide(BigDecimal.valueOf(100), 2,
                    java.math.RoundingMode.HALF_UP);

            BigDecimal taxableAmount = lineSubtotal.subtract(lineDiscount);
            BigDecimal taxRate = detail.getTaxRate() != null ? detail.getTaxRate() : BigDecimal.ZERO;
            BigDecimal lineTax = taxableAmount.multiply(taxRate).divide(BigDecimal.valueOf(100), 2,
                    java.math.RoundingMode.HALF_UP);

            subtotal = subtotal.add(lineSubtotal);
            totalDiscount = totalDiscount.add(lineDiscount);
            totalTax = totalTax.add(lineTax);
        }

        BigDecimal grandTotal = subtotal.subtract(totalDiscount).add(totalTax);

        // Mapping via MapStruct
        PurchaseOrderDetailResponse response = orderMapper.toPurchaseOrderDetailResponse(order);
        // Enrichment
        response.setSupplierName(supplierName);
        response.setSubtotal(subtotal);
        response.setTotalDiscount(totalDiscount);
        response.setTotalTax(totalTax);
        response.setGrandTotal(grandTotal);
        response.setItems(items);

        return response;
    }

    private void parseAmountRange(OrderSearchCriteria criteria) {

        if (criteria.getAmountRange() == null || criteria.getAmountRange().isBlank())
            return;
        switch (criteria.getAmountRange()) {
            case "0-5000" -> {
                criteria.setMinAmount(BigDecimal.ZERO);
                criteria.setMaxAmount(new BigDecimal("5000"));
            }
            case "5000-20000" -> {
                criteria.setMinAmount(new BigDecimal("5000"));
                criteria.setMaxAmount(new BigDecimal("20000"));
            }
            case "20000+" -> {
                criteria.setMinAmount(new BigDecimal("20000"));
                criteria.setMaxAmount(null);
            }
        }
    }
}
