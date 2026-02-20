package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dao.PurchaseDAO;
import edu.fpt.groupfive.dao.PurchaseDetailDAO;
import edu.fpt.groupfive.dto.request.PurchaseCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseSearchAndFilter;
import edu.fpt.groupfive.dto.response.PurchaseDetailResponse;
import edu.fpt.groupfive.dto.response.PurchaseResponse;
import edu.fpt.groupfive.mapper.PurchaseMapper;
import edu.fpt.groupfive.model.Purchase;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.PurchaseService;
import edu.fpt.groupfive.service.UserService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
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
    private final UserService userService;
    private final AssetTypeService assetTypeService;

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
    public PurchaseResponse findById(Integer id) {
            return purchaseDAO.findById(id)
                    .map(p -> PurchaseResponse.builder()
                            .id(p.getId())
                            .status(p.getStatus())
                            .creatorName(userService.findNameById(p.getCreatedByUser()))
                            .neededByDate(p.getNeededByDate())
                            .priority(p.getPriority().name())
                            .createdAt(p.getCreatedAt())
                            .purchaseDetails(
                                    p.getPurchaseDetails().stream()
                                            .map(pd -> PurchaseDetailResponse.builder()
                                                    .id(pd.getId())
                                                    .note(pd.getNote())
                                                    .estPrice(pd.getEstimatePrice())
                                                    .quantity(pd.getQuantity())
                                                    .specification(pd.getSpecificationRequirement())
                                                    .assetTypeName(
                                                            assetTypeService.findNameById(pd.getAssetTypeId())
                                                    )
                                                    .build()
                                            ).toList()
                            )
                            .build()
                    )
                    .orElseThrow(() -> new RuntimeException("Purchase tồn tại + id"));

    }

    @Override
    public List<PurchaseResponse> findAllPurchases() {

        return purchaseDAO.findAll().stream().map(p ->
                  PurchaseResponse.builder()
                          .id(p.getId())
                          .status(p.getStatus())
                          .creatorName(userService.findNameById(p.getCreatedByUser()))
                          .neededByDate(p.getNeededByDate())
                          .priority(p.getPriority().name())
                          .createdAt(p.getCreatedAt())
                          .purchaseDetails(p.getPurchaseDetails().stream().map(pd -> PurchaseDetailResponse.builder()
                                  .id(pd.getId())
                                  .note(pd.getNote())
                                  .estPrice(pd.getEstimatePrice())
                                  .quantity(pd.getQuantity())
                                  .specification(pd.getSpecificationRequirement())
                                  .assetTypeName(assetTypeService.findNameById(pd.getAssetTypeId()))
                                  .build()).toList())
                          .build()
          ).toList();

    }

    @Override
    public List<PurchaseResponse> searchAndFilter(PurchaseSearchAndFilter p) {

        if (p.getFrom() != null && p.getTo() != null
                && p.getFrom().isAfter(p.getTo())) {
            throw new InvalidDataException("From phải trước To");
        }

        return purchaseDAO.getPurchaseByFilter(p).stream().map(pr ->
                PurchaseResponse.builder()
                        .id(pr.getId())
                        .status(pr.getStatus())
                        .creatorName(userService.findNameById(pr.getCreatedByUser()))
                        .neededByDate(pr.getNeededByDate())
                        .priority(pr.getPriority().name())
                        .createdAt(pr.getCreatedAt())
                        .purchaseDetails(pr.getPurchaseDetails().stream().map(pd -> PurchaseDetailResponse.builder()
                                .id(pd.getId())
                                .note(pd.getNote())
                                .estPrice(pd.getEstimatePrice())
                                .quantity(pd.getQuantity())
                                .specification(pd.getSpecificationRequirement())
                                .assetTypeName(assetTypeService.findNameById(pd.getAssetTypeId()))
                                .build()).toList())
                        .build()
        ).toList();
    }

    @Override
    public void actionsWithPurchase(Integer purchaseId, String action, String reasonReject) {
        Purchase p = purchaseDAO.findById(purchaseId).orElseThrow(() -> new InvalidDataException("Purchase request " +
                "không tộn tại"));

        if ("a".equals(action)) {
            purchaseDAO.updatePurchaseStatus(Request.APPROVED, purchaseId, null);
        } else if ("r".equals(action)) {
            purchaseDAO.updatePurchaseStatus(Request.REJECTED, purchaseId, reasonReject);
        } else {
            throw new InvalidDataException("Action không hợp lệ: " + action);
        }
    }
}
