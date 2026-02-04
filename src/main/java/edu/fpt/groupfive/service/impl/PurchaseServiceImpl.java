package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dao.PurchaseDAO;
import edu.fpt.groupfive.dao.PurchaseDetailDAO;
import edu.fpt.groupfive.dto.request.PurchaseCreateRequest;
import edu.fpt.groupfive.mapper.PurchaseDetailMapper;
import edu.fpt.groupfive.mapper.PurchaseMapper;
import edu.fpt.groupfive.model.Purchase;
import edu.fpt.groupfive.model.PurchaseDetail;
import edu.fpt.groupfive.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "PURCHASE-SERVICE")
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseDAO purchaseDAO;
    private final PurchaseMapper purchaseMapper;
    private final PurchaseDetailDAO purchaseDetailDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPurchaseRequest(PurchaseCreateRequest purchaseCreateRequest, int userId) {
        log.info("Saving purchase request: {}", purchaseCreateRequest);

        //map sang purchase
        Purchase purchase = purchaseMapper.toPurchase(purchaseCreateRequest);

        //set các thuộc tính chưa map được
        purchase.setStatus(Request.PENDING);
        purchase.setCreatedByUser(userId);
        purchase.setCreatedAt(new Date());
        int purchaseId = purchaseDAO.insert(purchase);

        //insert purchase detail
        if(purchase.getPurchaseDetails() != null){

            purchase.getPurchaseDetails().forEach(d -> {
                d.setPurchaseRequestId(purchaseId);
                purchaseDetailDAO.insert(d);
            });
        }
        log.info("Saved");
    }
}
