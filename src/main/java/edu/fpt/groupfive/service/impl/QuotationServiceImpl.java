package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dao.PurchaseDAO;
import edu.fpt.groupfive.dao.PurchaseDetailDAO;
import edu.fpt.groupfive.dao.QuotationDAO;
import edu.fpt.groupfive.dao.QuotationDetailDAO;
import edu.fpt.groupfive.dto.request.QuotationCreateDetailRequest;
import edu.fpt.groupfive.dto.request.QuotationCreateRequest;
import edu.fpt.groupfive.mapper.QuotationDetailMapper;
import edu.fpt.groupfive.mapper.QuotationMapper;
import edu.fpt.groupfive.model.*;
import edu.fpt.groupfive.service.QuotationService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "QUOTATION-SERVICE")
public class QuotationServiceImpl implements QuotationService {

    private final QuotationDAO quotationDAO;
    private final QuotationDetailDAO quotationDetailDAO;
    private final PurchaseDAO purchaseDAO;
    private final PurchaseDetailDAO purchaseDetailDAO;
    private final QuotationMapper quotationMapper;
    private final QuotationDetailMapper quotationDetailMapper;

    @Override
    public void createQuotation(Integer purchaseId ,QuotationCreateRequest quotationCreateRequest) {

        // cheecjk purchase có tồn tại hay ko vs status la APPROVED
        Purchase purchase = purchaseDAO.findByIdAndApproved(purchaseId, "APPROVED")
                .orElseThrow(() -> new InvalidDataException("Purchase " +
                "request này không tồn tại"));

        List<PurchaseDetail> details =
                purchaseDetailDAO.findByPurchaseRequestId(purchaseId);

        // luu cac detail vao map de tien so sanh lieu purchaseDetail client gửi lên có thuộc purchase đang xử lí ko
        Map<Integer, PurchaseDetail> purchaseDetailMap = new HashMap<>();

        for (PurchaseDetail d : details) {
            purchaseDetailMap.put(d.getId(), d);
        }


        // tạo quotation
        Quotation q = quotationMapper.toQuotation(quotationCreateRequest);
        q.setPurchaseId(purchaseId);
        q.setStatus(edu.fpt.groupfive.common.QuotationStatus.PENDING);
        q.setCreatedAt(LocalDate.now());
        q.setUpdatedAt(LocalDate.now());
        
        // gắn supplier
        if (quotationCreateRequest.getSupplierId() != null) {
            Supplier s = new Supplier();
            s.setId(quotationCreateRequest.getSupplierId());
            q.setSupplier(s);
        }

        // tính total amount
        BigDecimal total = BigDecimal.ZERO;
        for (QuotationCreateDetailRequest item : quotationCreateRequest.getQuotationCreateDetailRequestList()) {
            if (item.getPrice() != null && item.getQuantity() != null) {
                total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        q.setTotalAmount(total);

        Integer quotationIdAfterInsert = quotationDAO.insert(q);

        // chèn từng từng purchase detail vào quotation detail
        if(quotationIdAfterInsert != 0){
            for(QuotationCreateDetailRequest qd : quotationCreateRequest.getQuotationCreateDetailRequestList()){
                PurchaseDetail pd = purchaseDetailMap.get(qd.getPurchaseRequestDetailId());

                // nếu purchase detail ko hợp lệ
                if(pd == null) throw  new InvalidDataException("purchaseDetailId koong hợp lệ");

                //map sang entity
                QuotationDetail quotationDetail = quotationDetailMapper.toQuotationDetail(qd);

                // gán quotationId và purchaseDetailId
                quotationDetail.setQuotationId(quotationIdAfterInsert);
                quotationDetail.setPurchaseDetailId(pd.getId());
                
                // set AssetType tu purchaseDetail
                if (pd.getAssetTypeId() != null) {
                    AssetType at = new AssetType();
                    at.setTypeId(pd.getAssetTypeId());
                    quotationDetail.setAssetType(at);
                }

                quotationDetailDAO.insert(quotationDetail);
            }

        }
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
            QuotationCreateDetailRequest item = QuotationCreateDetailRequest.builder()
                    .purchaseRequestDetailId(purchaseDetail.getId())
                    .quantity(purchaseDetail.getQuantity())
                    .build();

            quotationCreateDetailRequestList.add(item);
        }


        //trả bè quotation request
        return QuotationCreateRequest.builder()
                .purchaseRequestId(purchaseId)
                .quotationCreateDetailRequestList(quotationCreateDetailRequestList)
                .build();

    }
}
