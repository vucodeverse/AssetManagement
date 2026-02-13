package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.dao.AssetTypeDAO;
import edu.fpt.groupfive.dao.QuotationDAO;
import edu.fpt.groupfive.dao.QuotationDetailDAO;
import edu.fpt.groupfive.dao.SupplierDAO;
import edu.fpt.groupfive.dto.request.OrderCreateRequest;
import edu.fpt.groupfive.dto.request.OrderDetailCreateRequest;
import edu.fpt.groupfive.model.Quotation;
import edu.fpt.groupfive.model.QuotationDetail;
import edu.fpt.groupfive.service.OrderService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "ORDER-SERVICE")
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final QuotationDAO quotationDAO;
    private final QuotationDetailDAO quotationDetailDAO;
    private final AssetTypeDAO assetTypeDAO;
    private final SupplierDAO supplierDAO;

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
                    .assetTypeName(assetTypeDAO.findById(qd.getAssetTypeId()).getTypeName())
                    .quantity(qd.getQuantity())
                    .build());
        }

        // trả về orderReqeest
        return OrderCreateRequest.builder()
                .totalAmout(quotation.getTotalAmount())
                .supplierName(supplierDAO.findById(quotation.getSupplierId()).orElseThrow(() -> new InvalidDataException("Supllier không tồn tại")).getSupplierName())
                .quotationId(quotationId)
                .orderDetailCreateRequests(orderDetailCreateRequests)
                .build();
    }

    @Override
    public void createOrder(Integer quotationId, OrderCreateRequest orderCreateRequest) {

        // check
        Quotation quotation = quotationDAO.findById(quotationId).orElseThrow(() -> new InvalidDataException(
                "Quotation này ko tồn tại"));

        List<QuotationDetail> quotationDetails = quotationDetailDAO.findByQuotationId(quotationId);

        // key: quotationdetailid - value: quotationDetail
        Map<Integer, QuotationDetail> map =
                quotationDetails.stream().collect(Collectors.toMap(QuotationDetail::getQuotationId, x -> x));

    }
}
