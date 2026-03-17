package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.OrderStatus;
import edu.fpt.groupfive.common.QuotationStatus;
import edu.fpt.groupfive.dao.*;
// import edu.fpt.groupfive.dao.warehouse.WarehouseDAO;
import edu.fpt.groupfive.dto.request.PurchaseOrderCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseOrderDetailCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseOrderSearchCriteria;
import edu.fpt.groupfive.dto.response.PurchaseOrderDetailResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;
import edu.fpt.groupfive.mapper.OrderDetailMapper;
import edu.fpt.groupfive.mapper.OrderMapper;
import edu.fpt.groupfive.model.*;
// import edu.fpt.groupfive.model.warehouse.HandleStatus;
// import edu.fpt.groupfive.model.warehouse.InventoryTicket;
// import edu.fpt.groupfive.model.warehouse.TicketDetail;
// import edu.fpt.groupfive.model.warehouse.TicketType;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.ISupplierService;
import edu.fpt.groupfive.service.OrderService;
//import edu.fpt.groupfive.service.warehouse.InventoryTicketService;
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
    private final PurchaseDetailDAO purchaseDetailDAO;
    private final OrderCalculationUtil orderCalculationUtil;
    private final RangeAmount rangeAmount;
    private final ISupplierService supplierService;
    // private final WarehouseDAO warehouseDAO;
    // private final InventoryTicketService inventoryTicketService;
    private final UserDAO userDAO;

    @Value("${order.quotation_not_found}")
    private String quotationNotFoundMsg;

    @Value("${order.create.min_lines}")
    private String minLinesMsg;

    @Value("${order.create.quotation_detail_not_found}")
    private String quotationDetailNotFoundMsg;

    @Value("${order.create.pr_detail_not_found}")
    private String prDetailNotFoundMsg;

    @Value("${order.create.excess_quantity}")
    private String excessQuantityMsg;

    @Value("${order.create.failure}")
    private String failureMsg;

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
        List<QuotationDetail> quotationDetails = quotationDetailDAO.findByQuotationId(quotationId);
        String whName = null;
        // if (orderDAO.getWhIdFromPr(quotation.getPurchaseId()) != null) {
        // whName =
        // warehouseDAO.getById(orderDAO.getWhIdFromPr(quotation.getPurchaseId())).getName();
        // }

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
        List<QuotationDetail> quotationDetails = quotationDetailDAO.findByQuotationId(quotationId);

        // map dùng để check
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
                .collect(Collectors.toList());

        // lấy danh sách các prd id từ quotaiton detail id
        List<Integer> prDetailIds = quotationDetails.stream()
                .map(QuotationDetail::getPurchaseDetailId).toList();

        // Lấy số lượng đã đặt của từng dòng prd
        Map<Integer, Integer> orderedQtyByPrDetail = orderDAO.getOrderedQuantityByPurchaseDetailId(prDetailIds);

        // Lấy thông tin prd gốc để biết số lượng yêu cầu ban đầu
        List<PurchaseDetail> prDetails = purchaseDetailDAO.findByPurchaseRequestId(quotation.getPurchaseId());
        Map<Integer, PurchaseDetail> prDetailMap = prDetails.stream()
                .collect(Collectors.toMap(PurchaseDetail::getId, pd -> pd));

        Integer whId = 0; // warehouseDAO.getByName(purchaseOrderCreateRequest.getWarehouseName());

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

            // check xem có tồn tại quotaiton ứng với quotaitondetail id trong po cần tạo
            // hay ko
            QuotationDetail qd = quotationDetailMap.get(line.getQuotationDetailId());
            if (qd == null) {
                throw new InvalidDataException(quotationDetailNotFoundMsg);
            }

            // check xem có pr gốc của cái po này cần tạo hay ko
            PurchaseDetail prDetail = prDetailMap.get(qd.getPurchaseDetailId());
            if (prDetail == null) {
                throw new InvalidDataException(prDetailNotFoundMsg);
            }

            // lấy ra số lượng đã order của purcahserequestdetail id nầy
            int alreadyOrdered = orderedQtyByPrDetail.getOrDefault(qd.getPurchaseDetailId(), 0);

            // số lượng còn lại có thể order
            int remainingInPr = prDetail.getQuantity() - alreadyOrdered;

            if (line.getQuantity() > remainingInPr) {
                throw new InvalidDataException(excessQuantityMsg);
            }

            OrderDetail orderDetail = orderDetailMapper.toOrderDetail(line);

            orderDetail.setPrice(qd.getPrice());
            orderDetail.setTaxRate(qd.getTaxRate());
            orderDetail.setDiscountRate(qd.getDiscountRate());
            orderDetail.setAssetTypeId(qd.getAssetTypeId());

            orderDetails.add(orderDetail);

        }

        // 8. Update quotation status to APPROVED
        quotationDAO.updateStatus(quotationId, QuotationStatus.APPROVED, null);
        order.setOrderDetails(orderDetails);

        // insert
        Integer orderId = orderDAO.insert(order);
        if (orderId == null || orderId <= 0) {
            throw new InvalidDataException(failureMsg);
        }

        // sau khi PO được tạo thành công
        // InventoryTicket ticket = new InventoryTicket();
        // ticket.setWarehouseId(order.getWarehouseId());
        // ticket.setTicketType(TicketType.IN);
        // ticket.setStatus(HandleStatus.PENDING);

        // List<TicketDetail> ticketDetails = new ArrayList<>();
        // for (OrderDetail od : orderDetails) {
        // TicketDetail td = new TicketDetail();
        // td.setAssetTypeId(od.getAssetTypeId());
        // td.setQuantity(od.getQuantity());
        // td.setNote(od.getOrderDetailNote());
        // ticketDetails.add(td);
        // }

        // inventoryTicketService.createTicket(ticket, ticketDetails);
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

        // map sang po detail response
        List<PurchaseOrderDetailResponse> items = poDetails.stream()
                .map(detail -> {
                    PurchaseOrderDetailResponse itemDto = orderDetailMapper.toOrderDetailResponse(detail);
                    itemDto.setAssetTypeName(assetTypeNames.getOrDefault(detail.getAssetTypeId(), notFoundFallbackMsg));
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
        List<OrderDetail> list = orderDetailDAO.findAll();

        return null;
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
