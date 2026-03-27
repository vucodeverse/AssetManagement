package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.PurchaseProcessStatus;


import edu.fpt.groupfive.dao.*;
import edu.fpt.groupfive.dto.request.PurchaseOrderCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseOrderDetailCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseOrderSearchCriteria;
import edu.fpt.groupfive.dto.response.PurchaseOrderDetailResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;
import edu.fpt.groupfive.mapper.OrderDetailMapper;
import edu.fpt.groupfive.mapper.OrderMapper;
import edu.fpt.groupfive.model.*;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.ISupplierService;
import edu.fpt.groupfive.service.OrderService;
import edu.fpt.groupfive.service.UserService;
import edu.fpt.groupfive.util.OrderCalculationUtil;
import edu.fpt.groupfive.util.RangeAmount;
import edu.fpt.groupfive.util.SecurityUtils;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final QuotationDAO quotationDAO;
    private final QuotationDetailDAO quotationDetailDAO;
    private final OrderDetailMapper orderDetailMapper;
    private final OrderMapper orderMapper;
    private final OrderDAO orderDAO;
    private final OrderDetailDAO orderDetailDAO;
    private final AssetTypeService assetTypeService;
    private final OrderCalculationUtil orderCalculationUtil;
    private final RangeAmount rangeAmount;
    private final ISupplierService supplierService;
    private final UserService userService;
    private final UserDAO userDAO;
    private final PurchaseDAO purchaseDAO;
    private final PurchaseDetailDAO purchaseDetailDAO;
    private final SecurityUtils securityUtils;

    @Value("${order.quotation_not_found}")
    private String quotationNotFoundMsg;

    @Value("${order.create.min_lines}")
    private String minLinesMsg;

    @Value("${order.create.quotation_detail_not_found}")
    private String quotationDetailNotFoundMsg;

    @Value("${order.not_found}")
    private String orderNotFoundMsg;

    @Value("${order.delivery_date.required}")
    private String deliveryDateRequiredMsg;

    @Value("${order.delivery_date.past}")
    private String deliveryDatePastMsg;

    @Value("${order.delivery_date.invalid_format}")
    private String deliveryDateInvalidFormatMsg;

    @Value("${order.not_found_fallback}")
    private String notFoundFallbackMsg;

    // kiểm tra order hợp lệ hay ko
    @Override
    public PurchaseOrderCreateRequest preparePurchaseOrderForm(Integer quotationId) {

        // check quotation có đang tồn tịaij hay ko
        Quotation quotation = quotationDAO.findById(quotationId)
                .orElseThrow(() -> new InvalidDataException(quotationNotFoundMsg));
        // lấy ra list quotation detiail
        List<QuotationDetail> quotationDetails = quotationDetailDAO.findByQuotationId(quotationId).stream()
                .filter(qd -> PurchaseProcessStatus.APPROVED == qd.getQuotationDetailStatus()
                        || PurchaseProcessStatus.PENDING == qd.getQuotationDetailStatus())
                .toList();

        // lấy ra assettype
        Map<Integer, String> assetTypeNames = assetTypeService.getAssetTypeIdToNameMap();

        // map quotation detial sang purchase orderdetail
        List<PurchaseOrderDetailCreateRequest> purchaseOrderDetailCreateRequests = quotationDetails.stream()
                .map(qd -> PurchaseOrderDetailCreateRequest.builder()
                        .quotationDetailId(qd.getId())
                        .discountRate(qd.getDiscountRate())
                        .taxRate(qd.getTaxRate())
                        .price(qd.getPrice())
                        .assetTypeId(qd.getAssetTypeId())
                        .assetTypeName(assetTypeNames.getOrDefault(qd.getAssetTypeId(), notFoundFallbackMsg))
                        .quantity(qd.getQuantity())
                        .build())
                .toList();

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
    public Integer createPurchaseOrder(Integer quotationId, PurchaseOrderCreateRequest purchaseOrderCreateRequest,
            String username) {

        // ktra tồn tại
        Quotation quotation = quotationDAO.findById(quotationId)
                .orElseThrow(() -> new InvalidDataException(quotationNotFoundMsg));

        // ly toàn bộ quotation detail lên trước
        List<QuotationDetail> quotationDetails = quotationDetailDAO.findByQuotationId(quotationId).stream()
                .filter(qd -> PurchaseProcessStatus.APPROVED == qd.getQuotationDetailStatus()
                        || PurchaseProcessStatus.PENDING == qd.getQuotationDetailStatus())
                .toList();

        // lưu id và quota detail của nó
        Map<Integer, QuotationDetail> quotationDetailMap = quotationDetails.stream()
                .collect(Collectors.toMap(QuotationDetail::getId, qd -> qd));

        // check danh sách order request gửi về để tạo po
        List<PurchaseOrderDetailCreateRequest> lines = purchaseOrderCreateRequest
                .getPurchaseOrderDetailCreateRequests();
        if (lines == null || lines.isEmpty()) {
            throw new InvalidDataException(minLinesMsg);
        }

        // bỏ những row có quantity null và <= 0
        List<PurchaseOrderDetailCreateRequest> detailCreateRequests = lines.stream()
                .filter(o -> o.getQuantity() != null && o.getQuantity() > 0)
                .toList();

        // tạo order
        Order order = new Order();
        order.setQuotationId(quotationId);
        order.setPurchaseId(quotation.getPurchaseId());
        order.setOrderStatus(PurchaseProcessStatus.PENDING);
        order.setOrderNote(purchaseOrderCreateRequest.getOrderNote());
        order.setSupplierId(quotation.getSupplierId());
        order.setApprovedBy(userDAO.findUserIdByUsername(username));

        // tính lại tổng tiền
        orderCalculationUtil.recalculateTotal(purchaseOrderCreateRequest);
        order.setTotalAmount(purchaseOrderCreateRequest.getTotalAmount());

        List<OrderDetail> orderDetails = new ArrayList<>();

        // tạo từng order detail
        for (PurchaseOrderDetailCreateRequest line : detailCreateRequests) {

            // lấy thông tin báo giá chi tiết
            QuotationDetail qd = quotationDetailMap.get(line.getQuotationDetailId());
            if (qd == null) {
                throw new InvalidDataException(quotationDetailNotFoundMsg);
            }

            OrderDetail orderDetail = orderDetailMapper.toOrderDetail(line);
            orderDetail.setPrice(qd.getPrice());
            orderDetail.setTaxRate(qd.getTaxRate());
            orderDetail.setDiscountRate(qd.getDiscountRate());
            orderDetail.setAssetTypeId(qd.getAssetTypeId());
            orderDetail.setDeliveryDate(LocalDate.now().plusDays(7));

            orderDetails.add(orderDetail);
        }

        order.setOrderDetails(orderDetails);

        // insert order vao DB
        Integer orderId = orderDAO.insert(order);

        // update quotation detail sang APPROVED
        detailCreateRequests
                .forEach(line -> quotationDetailDAO.update(line.getQuotationDetailId(), PurchaseProcessStatus.APPROVED));

        // update quotation sang APPROVED
        quotationDAO.updateStatus(quotationId, PurchaseProcessStatus.APPROVED);

        // chuyển những quotation detail khác sang reject
        updateQuotationDetail(detailCreateRequests, quotationDetails);

        if(checkQuantityOfPO(quotation.getPurchaseId())) {
            purchaseDAO.updateStatus(PurchaseProcessStatus.ORDERED, quotation.getPurchaseId(), null, order.getApprovedBy());

            rejectOtherQuotations(quotationId, quotation.getPurchaseId());
        }
        return orderId;
    }

    private void updateQuotationDetail(List<PurchaseOrderDetailCreateRequest> approvedRequests, List<QuotationDetail> allCandidates) {
        Set<Integer> approvedIds = approvedRequests.stream()
                .map(PurchaseOrderDetailCreateRequest::getQuotationDetailId)
                .collect(Collectors.toSet());

        for (QuotationDetail qd : allCandidates) {
            if (!approvedIds.contains(qd.getId())) {
                switch (qd.getQuotationDetailStatus()) {
                    case DRAFT:
                        qd.setQuotationDetailStatus(PurchaseProcessStatus.DELETED);
                    default:
                        quotationDetailDAO.update(qd.getId(), PurchaseProcessStatus.REJECTED);
                }
            }
        }
    }

    // hủy những quotation khác khi hàng đã đủ
    private void rejectOtherQuotations(Integer currentQuotationId, Integer purchaseId) {
        List<Quotation> otherQuotations = quotationDAO.findByPurchaseId(purchaseId).stream()
                .filter(q -> !q.getId().equals(currentQuotationId))
                .filter(q -> q.getQuotationStatus() == PurchaseProcessStatus.PENDING
                        || q.getQuotationStatus() == PurchaseProcessStatus.DRAFT)
                .toList();

        for (Quotation q : otherQuotations) {

            List<QuotationDetail> details = quotationDetailDAO.findByQuotationId(q.getId());
            switch (q.getQuotationStatus()) {
                case PENDING:
                    quotationDAO.updateStatus(q.getId(), PurchaseProcessStatus.REJECTED);

                    for (QuotationDetail qd : details) {
                        if (qd.getQuotationDetailStatus() != PurchaseProcessStatus.REJECTED) {
                            quotationDetailDAO.update(qd.getId(), PurchaseProcessStatus.REJECTED);
                        }
                    }
                    break;
                case DRAFT:
                    quotationDAO.updateStatus(q.getId(), PurchaseProcessStatus.DELETED);

                    for (QuotationDetail qd : details) {
                        if (qd.getQuotationDetailStatus() != PurchaseProcessStatus.DELETED) {
                            quotationDetailDAO.update(qd.getId(), PurchaseProcessStatus.DELETED);
                        }
                    }
                    break;
            }
        }
    }

    // lấy ra toàn bộ po và pr id tương ứng
    @Override
    public List<PurchaseOrderResponse> searchPurchaseOrders(PurchaseOrderSearchCriteria criteria) {

        // set mặc điịnh min max
        criteria.setMinAmount(null);
        criteria.setMaxAmount(null);

        if (criteria.getAmountRange() != null && !criteria.getAmountRange().isBlank()) {
            List<BigDecimal> list = rangeAmount.applyRangeAMount(criteria.getAmountRange());

            if (list.size() == 1) {
                criteria.setMinAmount(list.get(0));
            } else if (list.size() == 2) {
                criteria.setMinAmount(list.get(0));
                criteria.setMaxAmount(list.get(list.size() - 1));
            }
        }

        List<Object[]> results = orderDAO.search(criteria);
        List<PurchaseOrderResponse> list = new ArrayList<>();

        // map từng row sang po response
        for (Object[] row : results) {
            Order order = (Order) row[0];
            String supplierName = (String) row[1];
            PurchaseOrderResponse poResponse = orderMapper.toPurchaseOrderResponse(order);
            poResponse.setSupplierName(supplierName);
            list.add(poResponse);
        }

        return list;
    }

    // lấy ra toàn bộ po detail
    @Override
    public PurchaseOrderResponse getPurchaseOrderById(Integer orderId) {
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new InvalidDataException(orderNotFoundMsg));

        Map<Integer, String> map = supplierService.getSupplierIdToNameMap();
        Map<Integer, String> assetTypeNames = assetTypeService.getAssetTypeIdToNameMap();
        Map<Integer, String> userMap = userService.getUserIdToUsernameMap();


        // lấy ra list po detail theo po id
        List<OrderDetail> poDetails = orderDetailDAO.findByOrderId(orderId);

        // Fetch quotation details to get purchaseRequestDetailId
        List<QuotationDetail> qDetails = quotationDetailDAO.findByQuotationId(order.getQuotationId());
        Map<Integer, Integer> qDetailIdToPurchaseDetailId = qDetails.stream()
                .collect(java.util.stream.Collectors.toMap(QuotationDetail::getId,
                        QuotationDetail::getPurchaseDetailId));

        // map sang po detail response
        List<PurchaseOrderDetailResponse> items = poDetails.stream()
                .map(detail -> {
                    PurchaseOrderDetailResponse itemDto = orderDetailMapper.toOrderDetailResponse(detail);
                    itemDto.setAssetTypeName(assetTypeNames.getOrDefault(detail.getAssetTypeId(), notFoundFallbackMsg));
                    itemDto.setPurchaseRequestDetailId(qDetailIdToPurchaseDetailId.get(detail.getQuotationDetailId()));
                    return itemDto;
                })
                .toList();

        // tính toán các loại giá cho order detail đó
        BigDecimal[] calculated = orderCalculationUtil.calculatePoDetail(poDetails);

        PurchaseOrderResponse response = orderMapper.toPurchaseOrderResponse(order);
        response.setSupplierName(map.getOrDefault(order.getSupplierId(), notFoundFallbackMsg));
        response.setApprovedByName(userMap.getOrDefault(order.getApprovedBy(), notFoundFallbackMsg));
        response.setOrderDetails(items);
        response.setSubtotal(calculated[0]);
        response.setTotalDiscount(calculated[1]);
        response.setTotalTax(calculated[2]);
        response.setTotalAmount(calculated[3]);

        return response;
    }

    @Override
    public void updateDeliveryDate(Integer orderId, String deliveryDateStr) {
        if (deliveryDateStr == null || deliveryDateStr.isBlank()) {
            throw new InvalidDataException(deliveryDateRequiredMsg);
        }

        LocalDate deliveryDate = LocalDate.parse(deliveryDateStr);
        if (deliveryDate.isBefore(LocalDate.now())) {
            throw new InvalidDataException(deliveryDatePastMsg);
        }
        try {
            orderDetailDAO.updateDeliveryDate(orderId, deliveryDate);
        } catch (Exception e) {
            throw new InvalidDataException(deliveryDateInvalidFormatMsg);
        }
    }

    @Override
    public List<PurchaseOrderDetailResponse> getAllOrderDetails() {
        List<OrderDetail> list=orderDetailDAO.findAll();
        return orderDetailMapper.toListOrderDetailResponse(list);
    }

    @Override
    public List<PurchaseOrderDetailResponse> getAllOrderDetails(Integer orderId) {
        Map<Integer, String> map = assetTypeService.getAssetTypeIdToNameMap();

        return orderDetailDAO.findByOrderId(orderId).stream()
                .map(pod -> PurchaseOrderDetailResponse.builder()
                        .purchaseOrderDetailId(pod.getId())
                        .price(pod.getPrice())
                        .taxRate(pod.getTaxRate())
                        .deliveryDate(pod.getDeliveryDate())
                        .discountRate(pod.getDiscountRate())
                        .quantity(pod.getQuantity())
                        .receivedQuantity(pod.getReceivedQuantity())
                        .assetTypeName(map.getOrDefault(pod.getAssetTypeId(), "Không có loại tài sản"))
                        .build()).toList();
    }

    @Override
    public List<PurchaseOrderResponse> getOrderWithPending() {

        Map<Integer, String> map = userService.getUserIdToUsernameMap();
        Map<Integer, String> supplierMap = supplierService.getSupplierIdToNameMap();

        return (List<PurchaseOrderResponse>) orderDAO.findRecent().stream().filter(o -> PurchaseProcessStatus.PENDING == o.getOrderStatus()).map(o -> PurchaseOrderResponse.builder()
                        .orderId(o.getId())
                .orderNote(o.getOrderNote())
                .createdAt(o.getCreatedAt())
                .orderStatus(o.getOrderStatus().name())
                .purchaseId(o.getPurchaseId())
                .supplierName(supplierMap.getOrDefault(o.getSupplierId(), "Không xác định"))
                .approvedByName(map.getOrDefault(o.getApprovedBy(), "Hiện chưa được chấp nhận"))
                .totalAmount(o.getTotalAmount()).build()).toList();
    }

    @Override
    public List<PurchaseOrderResponse> getInboundOrders() {
        Map<Integer, String> map = userService.getUserIdToUsernameMap();
        Map<Integer, String> supplierMap = supplierService.getSupplierIdToNameMap();

        return (List<PurchaseOrderResponse>) orderDAO.findRecent().stream()
                .filter(o -> PurchaseProcessStatus.PENDING == o.getOrderStatus() 
                          || PurchaseProcessStatus.PARTIALLY_RECEIVED == o.getOrderStatus()
                          || PurchaseProcessStatus.COMPLETED == o.getOrderStatus())
                .map(o -> PurchaseOrderResponse.builder()
                        .orderId(o.getId())
                        .orderNote(o.getOrderNote())
                        .createdAt(o.getCreatedAt())
                        .orderStatus(o.getOrderStatus().name())
                        .purchaseId(o.getPurchaseId())
                        .supplierName(supplierMap.getOrDefault(o.getSupplierId(), "Không xác định"))
                        .approvedByName(map.getOrDefault(o.getApprovedBy(), "Hiện chưa được chấp nhận"))
                        .totalAmount(o.getTotalAmount())
                        .build())
                .toList();
    }

    @Override
    public void updateStatus(Integer orderId, PurchaseProcessStatus orderStatus) {
        Order order = orderDAO.findById(orderId).orElseThrow(() -> new InvalidDataException(orderNotFoundMsg));

        if(checkQuantityOfPO(order.getPurchaseId()))
            purchaseDAO.updateStatus(PurchaseProcessStatus.COMPLETED, order.getPurchaseId(), null, securityUtils.getCurrentUserId());

        orderDAO.updateStatus(orderId, orderStatus);
    }

    private Boolean checkQuantityOfPO(Integer purchaseId){

        List<PurchaseDetail> purchaseDetails = purchaseDetailDAO.findByPurchaseRequestId(purchaseId);

        Map<Integer, Integer> ordered = orderDAO.getOrderedQuantityByPurchaseDetailId(purchaseDetails);

        for(PurchaseDetail pd : purchaseDetails){
            int orderedQty = ordered.getOrDefault(pd.getId(), 0);

            if(orderedQty < pd.getQuantity()){
                return false;
            }
        }

        return true;
    }


}
