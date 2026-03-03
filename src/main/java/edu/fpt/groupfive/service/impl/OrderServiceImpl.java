package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.OrderStatus;
import edu.fpt.groupfive.common.QuotationStatus;
import edu.fpt.groupfive.dao.*;
import edu.fpt.groupfive.dto.request.PurchaseOrderCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseOrderDetailCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseOrderSearchCriteria;
import edu.fpt.groupfive.dto.response.PurchaseOrderDetailResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderFullResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderGroupResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;
import edu.fpt.groupfive.mapper.OrderDetailMapper;
import edu.fpt.groupfive.mapper.OrderMapper;
import edu.fpt.groupfive.model.*;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.OrderService;
import edu.fpt.groupfive.util.OrderCalculationUtil;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final SupplierDAO supplierDAO;
    private final AssetTypeService assetTypeService;
    private final PurchaseDetailDAO purchaseDetailDAO;
    private final OrderCalculationUtil orderCalculationUtil;

    // kiểm tra order hợp lệ hay ko
    @Override
    public PurchaseOrderCreateRequest checkFormCreateOrder(Integer quotationId) {
        Quotation quotation = quotationDAO.findById(quotationId)
                .orElseThrow(() -> new InvalidDataException("Quotation không tồn tại: " + quotationId));

        // lấy ra list quotation detiail
        List<QuotationDetail> quotationDetails = quotationDetailDAO.findByQuotationId(quotationId);

        // lấy ra assettpe
        Map<Integer, String> assetTypeNames = assetTypeService.getAssetTypeIdToNameMap();

        // map quotation detial sang purchase orderdetail
        List<PurchaseOrderDetailCreateRequest> purchaseOrderDetailCreateRequests = quotationDetails.stream()
                .map(qd -> PurchaseOrderDetailCreateRequest.builder()
                        .quotationDetailId(qd.getId())
                        .discountRate(qd.getDiscountRate())
                        .taxRate(qd.getTaxRate())
                        .price(qd.getPrice())
                        .assetTypeId(qd.getAssetTypeId())
                        .assetTypeName(assetTypeNames.getOrDefault(qd.getAssetTypeId(), "N/A"))
                        .quantity(qd.getQuantity())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));

        // trả về order create
        return PurchaseOrderCreateRequest.builder()
                .totalAmount(quotation.getTotalAmount())
                .supplierId(quotation.getSupplierId())
                .quotationId(quotationId)
                .purchaseOrderDetailCreateRequests(purchaseOrderDetailCreateRequests)
                .build();
    }

    // tạo order
    @Override
    public Integer createOrder(Integer quotationId, PurchaseOrderCreateRequest purchaseOrderCreateRequest) {

        // ktra tồn tại
        Quotation quotation = quotationDAO.findById(quotationId)
                .orElseThrow(() -> new InvalidDataException("Quotation không tồn tại: " + quotationId));

        // ly toàn bộ quotation detail lên trước
        List<QuotationDetail> quotationDetails = quotationDetailDAO.findByQuotationId(quotationId);

        // map dùng để check
        Map<Integer, QuotationDetail> quotationDetailMap = quotationDetails.stream()
                .collect(Collectors.toMap(QuotationDetail::getId, qd -> qd));

        // check danh sách order request gửi về để tạo po
        List<PurchaseOrderDetailCreateRequest> lines = purchaseOrderCreateRequest
                .getPurchaseOrderDetailCreateRequests();
        if (lines == null || lines.isEmpty()) {
            throw new InvalidDataException("Cần ít nhất 1 dòng để tạo PO");
        }

        // bỏ những row có quantity null và <= 0
        List<PurchaseOrderDetailCreateRequest> detailCreateRequests = lines.stream()
                .filter(o -> o.getQuantity() != null && o.getQuantity() > 0)
                .collect(Collectors.toList());

        // lấy danh sách các prd id từ quotaiton detail id
        List<Integer> prDetailIds = quotationDetails.stream()
                .map(QuotationDetail::getPurchaseDetailId).toList();

        // Lấy số lượng đã đặt của từng dòng prd
        Map<Integer, Integer> orderedQtyByPrDetail = orderDAO.getOrderedQtyByPurchaseDetail(prDetailIds);

        // Lấy thông tin prd gốc để biết số lượng yêu cầu ban đầu
        List<PurchaseDetail> prDetails = purchaseDetailDAO.findByPurchaseRequestId(quotation.getPurchaseId());
        Map<Integer, PurchaseDetail> prDetailMap = prDetails.stream()
                .collect(Collectors.toMap(PurchaseDetail::getId, pd -> pd));

        // tạo order
        Order order = new Order();
        order.setQuotationId(quotationId);
        order.setPurchaseRequestId(quotation.getPurchaseId());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderNote(purchaseOrderCreateRequest.getOrderNote());
        order.setSupplierId(quotation.getSupplierId());

        // tính lại tổng tiền
        orderCalculationUtil.recalculateTotal(purchaseOrderCreateRequest);
        order.setTotalAmount(purchaseOrderCreateRequest.getTotalAmount());

        List<OrderDetail> orderDetails = new ArrayList<>();

        // tạo từng order detail
        for (PurchaseOrderDetailCreateRequest line : detailCreateRequests) {

            // check xem có tồn tại quotaiton ứng với quotaitondetail id trong po cần tạo
            // hay ko
            QuotationDetail qd = quotationDetailMap.get(line.getQuotationDetailId());
            if (qd == null) {
                throw new InvalidDataException("Quotation detail không tồn tại: " + line.getQuotationDetailId());
            }

            // check xem có pr gốc của cái po này cần tạo hay ko
            PurchaseDetail prDetail = prDetailMap.get(qd.getPurchaseDetailId());
            if (prDetail == null) {
                throw new InvalidDataException("Không tìm thấy thông tin Purchase Request gốc cho dòng này.");
            }

            // lấy ra số lượng đã order của purcahserequestdetail id nầy
            int alreadyOrdered = orderedQtyByPrDetail.getOrDefault(qd.getPurchaseDetailId(), 0);

            // số lượng còn lại có thể order
            int remainingInPr = prDetail.getQuantity() - alreadyOrdered;

            if (line.getQuantity() > remainingInPr) {
                throw new InvalidDataException(String.format(
                        "Số lượng đặt hàng (%d) vượt quá số lượng còn lại trong PR (%d). Đã đặt trước đó: %d/%d.",
                        line.getQuantity(), remainingInPr, alreadyOrdered, prDetail.getQuantity()));
            }

            OrderDetail orderDetail = orderDetailMapper.toOrderDetail(line);

            orderDetail.setPrice(qd.getPrice());
            orderDetail.setTaxRate(qd.getTaxRate());
            orderDetail.setDiscountRate(qd.getDiscountRate());
            orderDetail.setAssetTypeId(qd.getAssetTypeId());

            orderDetails.add(orderDetail);

        }

        // 8. Update quotation status to APPROVED
        quotationDAO.updateStatusReject(quotationId, QuotationStatus.APPROVED, null);
        order.setOrderDetails(orderDetails);

        // insert
        Integer orderId = orderDAO.insert(order);
        if (orderId == null || orderId <= 0) {
            throw new InvalidDataException("Tạo purchase order thất bại");
        }

        return orderId;
    }

    @Override
    public List<PurchaseOrderResponse> getPurchaseOrdersFlat(PurchaseOrderSearchCriteria criteria) {
        parseAmountRange(criteria);

        List<Object[]> results = orderDAO.searchAndFilter(criteria);
        List<PurchaseOrderResponse> list = new ArrayList<>();

        for (Object[] row : results) {
            Order order = (Order) row[0];
            String supplierName = (String) row[1];

            PurchaseOrderResponse poResponse = orderMapper.toPurchaseOrderResponse(order);
            poResponse.setSupplierName(supplierName);
            list.add(poResponse);
        }

        return list;
    }

    @Override
    public PurchaseOrderFullResponse getOrderDetail(Integer orderId) {
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new InvalidDataException("Purchase Order không tồn tại: " + orderId));

        String supplierName = supplierDAO.findById(order.getSupplierId())
                .map(Supplier::getSupplierName)
                .orElse("N/A");

        List<OrderDetail> detailModels = orderDetailDAO.findByOrderId(orderId);
        Map<Integer, String> assetTypeNames = assetTypeService.getAssetTypeIdToNameMap();

        List<PurchaseOrderDetailResponse> items = detailModels.stream()
                .map(detail -> {
                    PurchaseOrderDetailResponse itemDto = orderDetailMapper.toOrderDetailResponse(detail);
                    itemDto.setAssetTypeName(assetTypeNames.getOrDefault(detail.getAssetTypeId(), "N/A"));
                    return itemDto;
                })
                .toList();

        // Use centralized calculation utility
        BigDecimal[] breakdown = orderCalculationUtil.calculateBreakdownForOrder(detailModels);

        PurchaseOrderFullResponse response = orderMapper.toPurchaseOrderFullResponse(order);
        response.setSupplierName(supplierName);
        response.setItems(items);
        response.setSubtotal(breakdown[0]);
        response.setTotalDiscount(breakdown[1]);
        response.setTotalTax(breakdown[2]);
        response.setTotalAmount(breakdown[3]);

        return response;
    }

    private void parseAmountRange(PurchaseOrderSearchCriteria criteria) {

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
