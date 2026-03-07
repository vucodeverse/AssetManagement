package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.OrderStatus;
import edu.fpt.groupfive.common.QuotationStatus;
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
import edu.fpt.groupfive.service.OrderService;
import edu.fpt.groupfive.service.SupplierService;
import edu.fpt.groupfive.util.OrderCalculationUtil;
import edu.fpt.groupfive.util.RangeAmount;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final SupplierService supplierService;

    // kiểm tra order hợp lệ hay ko
    @Override
    public PurchaseOrderCreateRequest checkFormCreateOrder(Integer quotationId) {

        // check quotation có đang tồn tịaij hay ko
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
                        .build()).toList() ;

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

    // lấy ra toàn bộ po và pr id tương ứng
    @Override
    public List<PurchaseOrderResponse> getPurchaseOrders(PurchaseOrderSearchCriteria criteria) {

        // set mặc điịnh min max
        criteria.setMinAmount(null);
        criteria.setMaxAmount(null);


        if(criteria.getAmountRange() != null && !criteria.getAmountRange().isBlank()) {
            List<BigDecimal> list = rangeAmount.applyRangeAMount(criteria.getAmountRange());

            if(list.size() == 1) {
                criteria.setMinAmount(list.get(0));
            }else if(list.size() == 2) {
                criteria.setMinAmount(list.get(0));
                criteria.setMaxAmount(list.get(list.size()-1));
            }
        }


        List<Object[]> results = orderDAO.searchAndFilter(criteria);
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
    public PurchaseOrderResponse getOrderDetail(Integer orderId) {
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new InvalidDataException("Purchase Order không tồn tại: " + orderId));


        Map<Integer, String> map = supplierService.getSupplierIdToNameMap();
        Map<Integer, String> assetTypeNames = assetTypeService.getAssetTypeIdToNameMap();

        // lấy ra list po detail theo po id
        List<OrderDetail> poDetails = orderDetailDAO.findByOrderId(orderId);

        // map sang po detail response
        List<PurchaseOrderDetailResponse> items = poDetails.stream()
                .map(detail -> {
                    PurchaseOrderDetailResponse itemDto = orderDetailMapper.toOrderDetailResponse(detail);
                    itemDto.setAssetTypeName(assetTypeNames.getOrDefault(detail.getAssetTypeId(), "N/A"));
                    return itemDto;
                })
                .toList();

        // tính toán các loại giá cho order detail đó
        BigDecimal[] calculated = orderCalculationUtil.calculatePoDetail(poDetails);

        PurchaseOrderResponse response = orderMapper.toPurchaseOrderResponse(order);
        response.setSupplierName(map.getOrDefault(order.getSupplierId(), "N/A"));
        response.setItems(items);
        response.setSubtotal(calculated[0]);
        response.setTotalDiscount(calculated[1]);
        response.setTotalTax(calculated[2]);
        response.setTotalAmount(calculated[3]);

        return response;
        }

        @Override
        public void updateDeliveryDate(Integer orderId, String deliveryDateStr) {
           if (deliveryDateStr == null || deliveryDateStr.isBlank()) {
            throw new InvalidDataException("Ngày giao hàng không được để trống");
           }

            LocalDate deliveryDate = LocalDate.parse(deliveryDateStr);
           if(deliveryDate.isBefore(LocalDate.now())) {
            throw new InvalidDataException("Ngày giao hàng không được trong quá khứ");
          }
           try {
            orderDetailDAO.updateDeliveryDate(orderId, deliveryDate);
          } catch (Exception e) {
            throw new InvalidDataException("Định dạng ngày giao hàng không hợp lệ: " + deliveryDateStr);
          }
    }

}
