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
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.PurchaseService;
import edu.fpt.groupfive.service.UserService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

        Map<Integer, String> userMap = userService.getUserIdToUsernameMap();
        Map<Integer, String> assetTypeMap = assetTypeService.getAssetTypeIdToNameMap();

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

                    // Sử dụng mapper và thiết lập thêm thông tin chi tiết (details) và số lượng báo
                    // giá (quotationCount)
                    PurchaseResponse resp = purchaseMapper.toPurchaseResponse(p);
                    resp.setCreatorName(userMap.getOrDefault(p.getCreatedByUser(), "Không tồn tại người dùng"));
                    resp.setPurchaseDetails(details);
                    resp.setQuotationCount(quotationDAO.countQuotationFromPurchaseId(p.getId()));
                    return resp;
                })
                .orElseThrow(() -> new InvalidDataException("Purchase request không tồn tại: " + id));
    }

    // lấy ra tấy cả purchase
    @Override
    public List<PurchaseResponse> findAllPurchases() {
        Map<Integer, String> userMap = userService.getUserIdToUsernameMap();

        return purchaseDAO.findAll().stream().map(p -> {
            PurchaseResponse resp = purchaseMapper.toPurchaseResponse(p);
            resp.setCreatorName(userMap.getOrDefault(p.getCreatedByUser(), "Không tồn tại người dùng"));
            resp.setQuotationCount(quotationDAO.countQuotationFromPurchaseId(p.getId()));
            return resp;
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

        Map<Integer, String> userMap = userService.getUserIdToUsernameMap();

        // Chuyển đổi sang PurchaseResponse để hiển thị trên giao diện
        return purchaseDAO.getPurchaseByFilter(p).stream()
                .map(pr -> {
                    PurchaseResponse resp = purchaseMapper.toPurchaseResponse(pr);
                    resp.setCreatorName(userMap.getOrDefault(pr.getCreatedByUser(), "Không tồn tại người dùng"));
                    resp.setQuotationCount(quotationDAO.countQuotationFromPurchaseId(pr.getId()));
                    return resp;
                })
                .toList();
    }

    // thực hiện save và draft
    @Override
    public void actionsWithPurchase(Integer purchaseId, String action, String reasonReject) {

        // check purchase có tồn tại hay ko
        purchaseDAO.findById(purchaseId).orElseThrow(() -> new InvalidDataException("Purchase request không tộn tại"));

        // Xử lý các hành động (actions) nhận được từ controller
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

        // Chuyển đổi danh sách chi tiết yêu cầu mua sắm (purchase details)
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
