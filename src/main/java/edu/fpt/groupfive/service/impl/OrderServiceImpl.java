package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.OrderStatus;
import edu.fpt.groupfive.dao.*;
import edu.fpt.groupfive.dto.request.OrderCreateRequest;
import edu.fpt.groupfive.dto.request.OrderDetailCreateRequest;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "ORDER-SERVICE")
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final QuotationDAO quotationDAO;
    private final QuotationDetailDAO quotationDetailDAO;
    private final OrderDetailMapper orderDetailMapper;
    private final OrderDAO orderDAO;
    private final OrderDetailDAO orderDetailDAO;

    // xử lí khi form dc map từ quotation sang purchase order
    @Override
    public OrderCreateRequest checkFormCreateOrder(Integer quotationId) {

        Quotation quotation = quotationDAO.findById(quotationId).orElseThrow(() -> new InvalidDataException(
                "Quotation này ko tồn tại"));

        // laáy danh sác quotation detail;
        List<QuotationDetail> quotationDetails = quotationDetailDAO.findByQuotationId(quotationId);

        // khởi tạo list danh sách orderDetailREqayest
        List<OrderDetailCreateRequest> orderDetailCreateRequests = new ArrayList<>();
        for(QuotationDetail qd : quotationDetails){

            // set data tu quotation detail sang order detail
            orderDetailCreateRequests.add(OrderDetailCreateRequest.builder()
                    .quotationDetailId(qd.getId())
                    .discountRate(qd.getDiscountRate())
                    .taxRate(qd.getTaxRate())
                    .price(qd.getPrice())
                    .assetTypeId(qd.getAssetTypeId())
                    .quantity(qd.getQuantity())
                    .build());
        }

        // trả về orderReqeest
        return OrderCreateRequest.builder()
                .totalAmout(quotation.getTotalAmount())
                .supplierId(quotation.getSupplierId())
                .quotationId(quotationId)
                .orderDetailCreateRequests(orderDetailCreateRequests)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(Integer quotationId, OrderCreateRequest orderCreateRequest) {

        // check
        Quotation quotation = quotationDAO.findById(quotationId).orElseThrow(() -> new InvalidDataException(
                "Quotation này ko tồn tại"));


        // load all quotationdetail của quotation này
        List<QuotationDetail> quotationDetails = quotationDetailDAO.findByQuotationId(quotationId);
        // key: quotationdetailid - value: quotationDetail
        Map<Integer, QuotationDetail> map =
                quotationDetails.stream().collect(Collectors.toMap(QuotationDetail::getId,
                x -> x));

        // bỏ những detail đã bị xóa
        if(orderCreateRequest.getOrderDetailCreateRequests() == null || orderCreateRequest.getOrderDetailCreateRequests().isEmpty()){
            throw new InvalidDataException("Không có dòng nào để tạo PO");
        }

        List<OrderDetailCreateRequest> orderDetailCreateRequests =
                orderCreateRequest.getOrderDetailCreateRequests().stream().filter(o -> o.getQuantity() != null && o.getQuantity() > 0).collect(Collectors.toList()); // loại những reauest có quantity < 0

        // check duplicate order detail
        Set<Integer> s = new HashSet<>();
        for(OrderDetailCreateRequest orderDetailCreateRequest : orderDetailCreateRequests){

            // tránh trùng quotation detail
            if(!s.add(orderDetailCreateRequest.getQuotationDetailId())) throw   new InvalidDataException("Quotation " +
                    "đã tồn tại");
        }


        if(orderDetailCreateRequests.isEmpty()){
            throw new InvalidDataException("Đã hết dòng quotationDetail để tạo PO");
        }


        // lấy ra list id của quotationDetail của quotation này
        List<Integer> quotationDetailIds =
                orderDetailCreateRequests.stream().map(OrderDetailCreateRequest::getQuotationDetailId).toList();

        // tính số lượng đã order của từng quotationDetail
        Map<Integer, Integer> orderedMap = orderDAO.getOrderedQtyByQuotationDetail(quotationDetailIds);

        // tạo order
        Order order = new Order();
        order.setQuotationId(quotationId);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderNote(orderCreateRequest.getOrderNote());
        order.setSupplierId(quotation.getSupplierId());

        // insert vào db
        Integer orderAfterInsert = orderDAO.insert(order);
        if(orderAfterInsert != null){

            // set quotationDetail vào orderDetail
            for(OrderDetailCreateRequest od : orderDetailCreateRequests){

                // lấy ra quotation detail từ trong map
                QuotationDetail quotationDetail = map.get(od.getQuotationDetailId());
                // nếu ko có trong map trước đó đẩy lỗi
                if(quotationDetail == null) throw new InvalidDataException("QUotation khoong toofn tai");

                // lấy ra số quantity đã order trước đó nếu có
                int ordered = orderedMap.getOrDefault(od.getQuotationDetailId(), 0);

                // tính số lượng còn có thể order dc
                int remaining = quotationDetail.getQuantity() - ordered;


                // check só lượng order hiện tại có đang lớn hơn số còn có thể mua dc ko
                if(od.getQuantity() > remaining) throw new InvalidDataException("Chỉ có thể order tối đa " + remaining);

                // mao sang entity
                OrderDetail orderDetail = orderDetailMapper.toOrderDetail(od);

                // set các giá trị có sẵn
                orderDetail.setQuotationDetailId(quotationDetail.getId());
                orderDetail.setDiscountRate(quotationDetail.getDiscountRate());
                orderDetail.setTaxRate(quotationDetail.getTaxRate());
                orderDetail.setPrice(quotationDetail.getPrice());
                orderDetail.setOrderId(orderAfterInsert);
                orderDetail.setOrderDetailNote(od.getOrderDetailNote());
                orderDetail.setQuantity(od.getQuantity());
                orderDetail.setExpectedDeliveryDate(od.getExpectedDeliveryDate());
                orderDetail.setAssetTypeId(quotationDetail.getAssetTypeId());

                orderDetailDAO.insetOrderDetail(orderDetail);
            }
        }


    }
}
