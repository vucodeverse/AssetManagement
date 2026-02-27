package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dao.PurchaseDAO;
import edu.fpt.groupfive.dao.QuotationDAO;
import edu.fpt.groupfive.dto.request.PurchaseCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseDetailCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseSearchAndFilter;
import edu.fpt.groupfive.dto.response.PurchaseDetailResponse;
import edu.fpt.groupfive.dto.response.PurchaseResponse;
import edu.fpt.groupfive.mapper.PurchaseMapper;
import edu.fpt.groupfive.model.Purchase;
import edu.fpt.groupfive.model.Users;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.PurchaseService;
import edu.fpt.groupfive.service.UserService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseDAO purchaseDAO;
    private final QuotationDAO quotationDAO;
    private final PurchaseMapper purchaseMapper;
    private final UserService userService;
    private final AssetTypeService assetTypeService;

    // tạo 1 purchase request
    @Override
    public Integer createPurchaseRequest(PurchaseCreateRequest purchaseCreateRequest, int userId, Request request) {

        // map từ dto sang purchase
        Purchase purchase = purchaseMapper.toPurchase(purchaseCreateRequest);

        // set status
        purchase.setStatus(request);

        Integer purchaseId;

        // check xem purchase này đã được tạo hay chưa (đang update draft)
        // nếu rồi lấy luôn id đó để update
        // không thì tạo mới
        if (purchaseCreateRequest.getPurchaseId() != null) {
            purchaseId = purchaseCreateRequest.getPurchaseId();
            purchase.setId(purchaseId);
            purchase.setUpdatedAt(LocalDate.now());
            purchaseDAO.update(purchase);
        } else {
            purchase.setCreatedByUser(userId);
            purchase.setCreatedAt(LocalDate.now());
            purchaseId = purchaseDAO.insert(purchase);
        }

        return purchaseId;
    }

    // lấy ra purchase theo id
    @Override
    public PurchaseResponse findById(Integer id) {

        // map user id qua username
        Map<Integer, String> userMap = new HashMap<>();
        for (Users u : userService.getAllUsers()) {
            userMap.put(u.getUserId(), u.getUsername());
        }

        // map asset type id qua asset type name
        Map<Integer, String> assetTypeMap = new HashMap<>();
        for (var at : assetTypeService.getAllAssetType()) {
            assetTypeMap.put(at.getTypeId(), at.getTypeName());
        }

        return purchaseDAO.findById(id)
                .map(p -> {
                    // map purchase details
                    List<PurchaseDetailResponse> details = p.getPurchaseDetails().stream()
                            .map(pd -> PurchaseDetailResponse.builder()
                                    .id(pd.getId())
                                    .assetTypeName(assetTypeMap.getOrDefault(pd.getAssetTypeId(),
                                            "Không tồn tại loại tài sản"))
                                    .quantity(pd.getQuantity())
                                    .estimatePrice(pd.getEstimatePrice())
                                    .specificationRequirement(pd.getSpecificationRequirement())
                                    .purchaseDetailNote(pd.getPurchaseDetailNote())
                                    .build())
                            .toList();

                    return PurchaseResponse.builder()
                            .id(p.getId())
                            .status(p.getStatus())
                            .createdAt(p.getCreatedAt())
                            .priority(p.getPriority().name())
                            .neededByDate(p.getNeededByDate())
                            .creatorName(userMap.getOrDefault(p.getCreatedByUser(), "Không tồn tại người dùng"))
                            .purchaseDetails(details)
                            .quotationCount(quotationDAO.countQuotationFromPurchaseId(p.getId()))
                            .rejectReason(p.getRejectReason())
                            .build();
                })
                .orElseThrow(() -> new InvalidDataException("Purchase request không tồn tại: " + id));
    }

    // lấy ra tấy cả purchase
    @Override
    public List<PurchaseResponse> findAllPurchases() {
        Map<Integer, String> map = new HashMap<>();

        for (Users u : userService.getAllUsers()) {
            map.put(u.getUserId(), u.getUsername());
        }

        return purchaseDAO.findAll().stream().map(p -> {
            return PurchaseResponse.builder()
                    .id(p.getId())
                    .status(p.getStatus())
                    .createdAt(p.getCreatedAt())
                    .priority(p.getPriority().name())
                    .neededByDate(p.getNeededByDate())
                    .creatorName(map.getOrDefault(p.getCreatedByUser(), "Không tồn tại người dùng"))
                    .quotationCount(quotationDAO.countQuotationFromPurchaseId(p.getId()))
                    .build();

        }).toList();
    }

    // thực hiện search và filter
    @Override
    public List<PurchaseResponse> searchAndFilter(PurchaseSearchAndFilter p) {

        // validate ngày tháng nhập vào có hợp lí ko
        if (p.getFrom() != null && p.getTo() != null
                && p.getFrom().isAfter(p.getTo())) {
            throw new InvalidDataException("From phải trước To");
        }

        // map sang Purchase response để hiển thị
        return purchaseDAO.getPurchaseByFilter(p).stream()
                .map(pr -> {
                    PurchaseResponse resp = purchaseMapper.toPurchaseResponse(pr);
                    resp.setQuotationCount(quotationDAO.countQuotationFromPurchaseId(pr.getId()));
                    return resp;
                })
                .toList();
    }

    // thực hiện save và draft
    @Override
    public void actionsWithPurchase(Integer purchaseId, String action, String reasonReject) {

        // check purchase có tồn tại hay ko
        purchaseDAO.findById(purchaseId).orElseThrow(() -> new InvalidDataException("Purchase request " +
                "không tộn tại"));

        // xử lí các trường hợp nhận về actions
        if ("a".equals(action)) {
            purchaseDAO.updatePurchaseStatus(Request.APPROVED, purchaseId, null);
        } else if ("r".equals(action)) {
            purchaseDAO.updatePurchaseStatus(Request.REJECTED, purchaseId, reasonReject);
        } else {
            throw new InvalidDataException("Action không hợp lệ: " + action);
        }
    }

    // lấy ra purchase đã save draft để sửa
    @Override
    public PurchaseCreateRequest loadDraftForEdit(Integer purchaseId) {
        Purchase purchase = purchaseDAO.findById(purchaseId)
                .orElseThrow(() -> new InvalidDataException("Purchase request không tồn tại: " + purchaseId));

        PurchaseCreateRequest request = new PurchaseCreateRequest();
        request.setPurchaseId(purchase.getId());
        request.setPurchaseNote(purchase.getPurchaseNote());
        request.setNeededByDate(purchase.getNeededByDate());
        request.setReason(purchase.getReason());
        request.setPriority(purchase.getPriority());

        // map purchase details
        if (purchase.getPurchaseDetails() != null) {
            List<PurchaseDetailCreateRequest> details = purchase.getPurchaseDetails().stream()
                    .map(pd -> PurchaseDetailCreateRequest.builder()
                            .assetTypeId(pd.getAssetTypeId())
                            .quantity(pd.getQuantity())
                            .estimatePrice(pd.getEstimatePrice())
                            .specificationRequirement(pd.getSpecificationRequirement())
                            .purchaseDetailNote(pd.getPurchaseDetailNote())
                            .build())
                    .toList();
            request.setPurchaseDetailCreateRequests(new java.util.ArrayList<>(details));
        }

        return request;
    }
}
