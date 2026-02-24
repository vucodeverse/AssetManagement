package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.OrderStatus;
import edu.fpt.groupfive.dao.*;
import edu.fpt.groupfive.dto.request.OrderCreateRequest;
import edu.fpt.groupfive.dto.request.OrderDetailCreateRequest;
import edu.fpt.groupfive.dto.request.OrderSearchCriteria;
import edu.fpt.groupfive.dto.response.PurchaseOrderGroupResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;
import edu.fpt.groupfive.mapper.OrderDetailMapper;
import edu.fpt.groupfive.mapper.OrderMapper;
import edu.fpt.groupfive.model.Order;
import edu.fpt.groupfive.model.OrderDetail;
import edu.fpt.groupfive.model.Quotation;
import edu.fpt.groupfive.model.QuotationDetail;
import edu.fpt.groupfive.service.OrderService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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

    // ── Build form data từ quotation ────────────────────────────────────────

    @Override
    public OrderCreateRequest checkFormCreateOrder(Integer quotationId) {
        Quotation quotation = quotationDAO.findById(quotationId)
                .orElseThrow(() -> new InvalidDataException("Quotation không tồn tại: " + quotationId));

        List<QuotationDetail> quotationDetails = quotationDetailDAO.findByQuotationId(quotationId);

        List<OrderDetailCreateRequest> orderDetailCreateRequests = quotationDetails.stream()
                .map(qd -> OrderDetailCreateRequest.builder()
                        .quotationDetailId(qd.getId())
                        .discountRate(qd.getDiscountRate())
                        .taxRate(qd.getTaxRate())
                        .price(qd.getPrice())
                        .assetTypeId(qd.getAssetTypeId())
                        .quantity(qd.getQuantity())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new)); // ArrayList để .remove() hoạt động

        return OrderCreateRequest.builder()
                .totalAmout(quotation.getTotalAmount())
                .supplierId(quotation.getSupplierId())
                .quotationId(quotationId)
                .orderDetailCreateRequests(orderDetailCreateRequests)
                .build();
    }

    // ── Tạo Purchase Order ─────────────────────────────────────────────────

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(Integer quotationId, OrderCreateRequest req) {

        // 1. Validate quotation tồn tại
        Quotation quotation = quotationDAO.findById(quotationId)
                .orElseThrow(() -> new InvalidDataException("Quotation không tồn tại: " + quotationId));

        // 2. Load toàn bộ QuotationDetail từ DB → dùng Map để tra cứu nhanh O(1)
        Map<Integer, QuotationDetail> quotationDetailMap = quotationDetailDAO.findByQuotationId(quotationId)
                .stream()
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

        // 5. Lấy số lượng đã order trước đó theo quotationDetailId
        List<Integer> detailIds = validLines.stream()
                .map(OrderDetailCreateRequest::getQuotationDetailId)
                .collect(Collectors.toList());
        Map<Integer, Integer> orderedQtyMap = orderDAO.getOrderedQtyByQuotationDetail(detailIds);

        // 6. Tạo Order header
        Order order = new Order();
        order.setQuotationId(quotationId);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderNote(req.getOrderNote());
        order.setSupplierId(quotation.getSupplierId());
        order.setTotalAmount(quotation.getTotalAmount());

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

            int alreadyOrdered = orderedQtyMap.getOrDefault(line.getQuotationDetailId(), 0);
            int remaining = qd.getQuantity() - alreadyOrdered;
            if (line.getQuantity() > remaining) {
                throw new InvalidDataException("Chỉ có thể order tối đa " + remaining + " cho detail " + line.getQuotationDetailId());
            }

            // Dùng mapper để map các field chung (quantity, note, expectedDeliveryDate, quotationDetailId)
            OrderDetail orderDetail = orderDetailMapper.toOrderDetail(line);

            // Override các field lấy từ DB (không tin form) để tránh tampering
            orderDetail.setPrice(qd.getPrice());
            orderDetail.setTaxRate(qd.getTaxRate());
            orderDetail.setDiscountRate(qd.getDiscountRate());
            orderDetail.setAssetTypeId(qd.getAssetTypeId());
            orderDetail.setOrderId(orderId);

            orderDetailDAO.insetOrderDetail(orderDetail);
        }
    }

    @Override
    public List<PurchaseOrderGroupResponse> getOrdersGroupedByPR(OrderSearchCriteria criteria) {
        parseAmountRange(criteria);

        List<Order> orders = orderDAO.searchAndFilter(criteria);

        LinkedHashMap<Integer, PurchaseOrderGroupResponse> groupMap = new LinkedHashMap<>();

        for (Order order : orders) {
            String raw = order.getPurchaseOrderNote();
            String supplierName = "";
            int purchaseRequestId = 0;

            if (raw != null && raw.contains("|")) {
                String[] parts = raw.split("\\|", 2);
                supplierName = parts[0];
                purchaseRequestId = Integer.parseInt(parts[1]);
            }

            PurchaseOrderResponse poResponse = orderMapper.toPurchaseOrderResponse(order);
            poResponse.setSupplierName(supplierName);

            groupMap.computeIfAbsent(purchaseRequestId, prId ->
                    PurchaseOrderGroupResponse.builder()
                            .purchaseRequestId(prId)
                            .orders(new ArrayList<>())
                            .build()
            ).getOrders().add(poResponse);
        }

        return new ArrayList<>(groupMap.values());
    }

    private void parseAmountRange(OrderSearchCriteria criteria) {
        if (criteria.getAmountRange() == null || criteria.getAmountRange().isBlank()) return;
        switch (criteria.getAmountRange()) {
            case "0-5000"       -> { criteria.setMinAmount(BigDecimal.ZERO);            criteria.setMaxAmount(new BigDecimal("5000")); }
            case "5000-20000"   -> { criteria.setMinAmount(new BigDecimal("5000"));     criteria.setMaxAmount(new BigDecimal("20000")); }
            case "20000+"       -> { criteria.setMinAmount(new BigDecimal("20000"));    criteria.setMaxAmount(null); }
        }
    }
}
