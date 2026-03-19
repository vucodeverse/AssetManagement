package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.OrderStatus;
import edu.fpt.groupfive.common.QuotationStatus;
import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dao.*;
import edu.fpt.groupfive.dao.warehouse.WarehouseDAO;
import edu.fpt.groupfive.dto.request.PurchaseOrderCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseOrderDetailCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseOrderSearchCriteria;
import edu.fpt.groupfive.dto.response.PurchaseOrderDetailResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;
import edu.fpt.groupfive.mapper.OrderDetailMapper;
import edu.fpt.groupfive.mapper.OrderMapper;
import edu.fpt.groupfive.model.*;
import edu.fpt.groupfive.model.warehouse.HandleStatus;
import edu.fpt.groupfive.model.warehouse.InventoryTicket;
import edu.fpt.groupfive.model.warehouse.TicketDetail;
import edu.fpt.groupfive.model.warehouse.TicketType;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.ISupplierService;
import edu.fpt.groupfive.service.OrderService;
import edu.fpt.groupfive.service.warehouse.InventoryTicketService;
import edu.fpt.groupfive.util.OrderCalculationUtil;
import edu.fpt.groupfive.util.RangeAmount;
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
    private final WarehouseDAO warehouseDAO;
    private final InventoryTicketService inventoryTicketService;
    private final UserDAO userDAO;
    private final PurchaseDAO purchaseDAO;
    private final PurchaseDetailDAO purchaseDetailDAO;

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
                .filter(qd -> QuotationStatus.APPROVED == qd.getQuotationDetailStatus()
                        || QuotationStatus.PENDING == qd.getQuotationDetailStatus())
                .toList();
        String whName = null;
        Integer existingWhId = orderDAO.getWhIdFromPr(quotation.getPurchaseId());
        if (existingWhId != null) {
            whName = warehouseDAO.getById(existingWhId).getName();
        }

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
                .warehouseName(whName)
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
                .filter(qd -> QuotationStatus.APPROVED == qd.getQuotationDetailStatus()
                        || QuotationStatus.PENDING == qd.getQuotationDetailStatus())
                .toList();

        // map dùng để lấy đơn giá, thuế... từ báo giá
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

        Integer whId = warehouseDAO.getByName(purchaseOrderCreateRequest.getWarehouseName());

        // tạo order
        Order order = new Order();
        order.setQuotationId(quotationId);
        order.setPurchaseId(quotation.getPurchaseId());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderNote(purchaseOrderCreateRequest.getOrderNote());
        order.setWarehouseId(whId);
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

            orderDetails.add(orderDetail);
        }

        order.setOrderDetails(orderDetails);

        // insert order vao DB
        Integer orderId = orderDAO.insert(order);

        // update quotation detail sang APPROVED
        detailCreateRequests
                .forEach(line -> quotationDetailDAO.update(line.getQuotationDetailId(), QuotationStatus.APPROVED));

        // update quotation sang APPROVED
        quotationDAO.updateStatus(quotationId, QuotationStatus.APPROVED, null);

        // tao inventory ticket cho warehouse nhan hang
        InventoryTicket ticket = new InventoryTicket();
        ticket.setWarehouseId(order.getWarehouseId());
        ticket.setTicketType(TicketType.IN);
        ticket.setStatus(HandleStatus.PENDING);

        List<TicketDetail> ticketDetails = orderDetails.stream()
                .map(od -> {
                    TicketDetail td = new TicketDetail();
                    td.setAssetTypeId(od.getAssetTypeId());
                    td.setQuantity(od.getQuantity());
                    td.setNote(od.getOrderDetailNote());
                    return td;
                })
                .collect(Collectors.toList());

        inventoryTicketService.createTicket(ticket, ticketDetails);
        if(checkQuantityOfPO(quotation.getPurchaseId())) purchaseDAO.updateStatus(Request.ORDERED, quotation.getPurchaseId(), null, order.getApprovedBy());
        return orderId;
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
        throw new UnsupportedOperationException("getAllOrderDetails() chua duoc implement");
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
