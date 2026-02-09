package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dao.PurchaseDAO;
import edu.fpt.groupfive.dao.PurchaseDetailDAO;
import edu.fpt.groupfive.dao.QuotationDAO;
import edu.fpt.groupfive.dto.request.QuotationCreateDetailRequest;
import edu.fpt.groupfive.dto.request.QuotationCreateRequest;
import edu.fpt.groupfive.model.Purchase;
import edu.fpt.groupfive.model.PurchaseDetail;
import edu.fpt.groupfive.service.QuotationService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "QUOTATION-SERVICE")
public class QuotationServiceImpl implements QuotationService {

    private final QuotationDAO quotationDAO;
    private final PurchaseDAO purchaseDAO;
    private final PurchaseDetailDAO purchaseDetailDAO;

    @Override
    public void createQuotation(Integer purchaseId ,QuotationCreateRequest quotationCreateRequest) {

        // cheecjk purchase có tồn tại hay ko vs status la APPROVED
        Purchase purchase = purchaseDAO.findByIdAndApproved(purchaseId, "APPROVED")
                .orElseThrow(() -> new InvalidDataException("Purchase " +
                "request này không tồn tại"));


    }

    @Override
    public QuotationCreateRequest checkFormQuotation(Integer purchaseId) {

        // load purchase request với status Approve
        Purchase purchase = purchaseDAO.findByIdAndApproved(purchaseId, Request.APPROVED.name())
                .orElseThrow(() -> new InvalidDataException("Purchase " +
                        "request này chưa được chấp nhận."));

        // load list detail nếu có
        List<PurchaseDetail> purchaseDetailList = purchaseDetailDAO.findByPurchaseRequestId(purchaseId);


        //list detail quotation
        List<QuotationCreateDetailRequest> quotationCreateDetailRequestList = new ArrayList<>();


        // chuyen tu purchase detail vào quotation create
        for(PurchaseDetail purchaseDetail : purchaseDetailList){
            QuotationCreateDetailRequest quotationCreateDetailRequest = new QuotationCreateDetailRequest();

            //set san id va quantity
            quotationCreateDetailRequest.builder()
                    .purchaseRequestDetailId(purchaseDetail.getId())
                    .quantity(purchaseDetail.getQuantity())
                    .build();

            quotationCreateDetailRequestList.add(quotationCreateDetailRequest);
        }


        //trả bè quotation request
        return QuotationCreateRequest.builder()
                .purchaseRequestId(purchaseId)
                .quotationCreateDetailRequestList(quotationCreateDetailRequestList)
                .build();

    }
}
