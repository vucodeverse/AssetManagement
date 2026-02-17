package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dao.PurchaseDAO;
import edu.fpt.groupfive.dao.PurchaseDetailDAO;
import edu.fpt.groupfive.dto.request.PurchaseCreateRequest;
import edu.fpt.groupfive.dto.response.PurchaseResponse;
import edu.fpt.groupfive.mapper.PurchaseMapper;
import edu.fpt.groupfive.model.Purchase;
import edu.fpt.groupfive.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    public void createPurchaseRequest(PurchaseCreateRequest purchaseCreateRequest, int userId, Request request) {
        log.info("Saving purchase request: {}", purchaseCreateRequest);

        //map sang purchase
        Purchase purchase = purchaseMapper.toPurchase(purchaseCreateRequest);

        //set các thuộc tính chưa map được
        purchase.setStatus(request);
        purchase.setCreatedByUser(2);
        purchase.setCreatedAt(LocalDate.now());


        Integer purchaseId = purchaseDAO.insert(purchase);

        //insert purchase detail
        if(purchase.getPurchaseDetails() != null && purchaseId != null){

            purchase.getPurchaseDetails().forEach(d -> {
                d.setPurchaseRequestId(purchaseId);
                purchaseDetailDAO.insert(d);
            });
        }
        log.info("Saved");
    }

    @Override
    public Purchase findById(Integer id) {
        return purchaseDAO.findById(id).orElseThrow(() -> new RuntimeException("Purchase not found"));
    }

    @Override
    public List<PurchaseResponse> findAllPurchases() {

         return purchaseDAO.findAll().stream().map(purchaseMapper::toPurchaseResponse).toList();

    }
}
