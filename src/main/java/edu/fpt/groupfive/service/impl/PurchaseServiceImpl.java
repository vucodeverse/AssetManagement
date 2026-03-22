package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.PurchaseProcessStatus;
import edu.fpt.groupfive.common.Role;
import edu.fpt.groupfive.dao.PurchaseDAO;
import edu.fpt.groupfive.dao.QuotationDAO;
import edu.fpt.groupfive.dto.request.PurchaseRequestCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseRequestDetailCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseRequestSearchCriteria;
import edu.fpt.groupfive.dto.response.PurchaseRequestDetailResponse;
import edu.fpt.groupfive.dto.response.PurchaseRequestResponse;
import edu.fpt.groupfive.mapper.PurchaseMapper;
import edu.fpt.groupfive.model.Purchase;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.PurchaseService;
import edu.fpt.groupfive.service.UserService;
import edu.fpt.groupfive.util.config.RoleLogin;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final RoleLogin roleLogin;

    @Value("${purchase.detail.asset_type_not_found}")
    private String assetTypeNotFoundMsg;

    @Value("${purchase.user_not_found}")
    private String userNotFoundMsg;

    @Value("${purchase.not_found}")
    private String purchaseNotFoundMsg;

    @Value("${purchase.search.invalid_date}")
    private String invalidDateMsg;

    @Value("${purchase.action.invalid_id}")
    private String invalidIdMsg;

    @Value("${purchase.action.invalid_action}")
    private String invalidActionMsg;

    @Value("${purchase.edit.not_draft}")
    private String notDraftMsg;

    // tạo 1 purchase purchaseProcessStatus
    @Override
    public Integer createPurchaseRequest(PurchaseRequestCreateRequest purchaseCreateRequest, int userId,
            PurchaseProcessStatus purchaseProcessStatus) {

        // map từ dto sang purchase
        Purchase purchase = purchaseMapper.toPurchase(purchaseCreateRequest);

        // set status
        purchase.setStatus(purchaseProcessStatus);

        Integer purchaseId;

        // check xem purchase này đã được tạo hay chưa
        // nếu rồi lấy luôn id đó để update
        // không thì tạo mới
        if (purchaseCreateRequest.getPurchaseId() != null) {
            purchaseId = purchaseCreateRequest.getPurchaseId();
            purchase.setId(purchaseId);
            purchase.setUpdatedAt(LocalDateTime.now());
            purchaseDAO.update(purchase);
        } else {
            purchase.setCreatedByUser(userId);
            purchase.setCreatedAt(LocalDateTime.now());
            purchaseId = purchaseDAO.insert(purchase);
        }

        // trả về id sau khi đã insert
        return purchaseId;
    }

    // lấy ra purchase theo id
    @Override
    public PurchaseRequestResponse getPurchaseRequestById(Integer id) {

        Map<Integer, String> userMap = userService.getUserIdToUsernameMap();
        Map<Integer, String> assetTypeMap = assetTypeService.getAssetTypeIdToNameMap();

        return purchaseDAO.findById(id)
                .map(p -> {
                    // map purchase details
                    List<PurchaseRequestDetailResponse> details = p.getPurchaseDetails().stream()
                            .map(pd -> PurchaseRequestDetailResponse.builder()
                                    .id(pd.getId())
                                    .assetTypeName(pd.getTypeId() == null ? assetTypeNotFoundMsg
                                            : assetTypeMap.getOrDefault(pd.getTypeId(), assetTypeNotFoundMsg))
                                    .quantity(pd.getQuantity())
                                    .estimatePrice(pd.getEstimatePrice())
                                    .specificationRequirement(pd.getSpecificationRequirement())
                                    .purchaseDetailNote(pd.getPurchaseDetailNote())
                                    .build())
                            .toList();

                    // tính total amount
                    BigDecimal totalAmount = p.getPurchaseDetails().stream()
                            .map(pd -> pd.getEstimatePrice().multiply(new BigDecimal(pd.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // map snag purchase response
                    PurchaseRequestResponse resp = purchaseMapper.toPurchaseResponse(p);
                    resp.setCreatorName(p.getCreatedByUser() == null ? userNotFoundMsg
                            : userMap.getOrDefault(p.getCreatedByUser(), userNotFoundMsg));
                    resp.setPurchaseDetails(details);
                    resp.setQuotationCount(quotationDAO.countQuotationFromPurchaseId(p.getId()));
                    resp.setTotalAmount(totalAmount);
                    return resp;
                })
                .orElseThrow(() -> new InvalidDataException(purchaseNotFoundMsg));
    }

    // lấy ra tấy cả purchase
    @Override
    public List<PurchaseRequestResponse> getPurchaseRequests() {

        // lấy ra tất first name và last name của use
        Map<Integer, String> userMap = userService.getUserIdToUsernameMap();

        List<Purchase> purchases = purchaseDAO.findAll();

        purchases = author(purchases);
        return purchases.stream().map(p -> {

            // map sang response để trả về client
            PurchaseRequestResponse resp = purchaseMapper.toPurchaseResponse(p);

            // lấy ra tên người dùng
            resp.setCreatorName(userMap.getOrDefault(p.getCreatedByUser(), userNotFoundMsg));
            return resp;
        }).toList();
    }

    // thực hiện search và filter
    @Override
    public List<PurchaseRequestResponse> searchPurchaseRequests(PurchaseRequestSearchCriteria p) {

        // validate ngày tháng nhập vào có hợp lí ko
        if (p.getFrom() != null && p.getTo() != null
                && p.getFrom().isAfter(p.getTo())) {
            throw new InvalidDataException(invalidDateMsg);
        }

        // lấy ra user
        Map<Integer, String> userMap = userService.getUserIdToUsernameMap();
        List<Purchase> purchases = purchaseDAO.search(p);

        purchases = author(purchases);

        return purchases.stream()
                .map(pr -> {

                    // Chuyển đổi sang PurchaseRequestResponse để hiển thị trên giao diện
                    PurchaseRequestResponse resp = purchaseMapper.toPurchaseResponse(pr);
                    resp.setCreatorName(userMap.getOrDefault(pr.getCreatedByUser(), userNotFoundMsg));
                    return resp;
                })
                .toList();
    }

    // thực hiện save và draft
    @Override
    public void processPurchaseRequestAction(Integer purchaseId, String action, String reasonReject, Integer userId) {

        // check purchase có tồn tại hay ko
        purchaseDAO.findById(purchaseId).orElseThrow(() -> new InvalidDataException(invalidIdMsg));

        // Xử lý các hành động (actions) nhận được từ controller
        if ("a".equals(action)) {
            purchaseDAO.updateStatus(PurchaseProcessStatus.APPROVED, purchaseId, null, userId);
        } else if ("r".equals(action)) {
            purchaseDAO.updateStatus(PurchaseProcessStatus.REJECTED, purchaseId, reasonReject, userId);
        } else if ("d".equals(action)) {
            purchaseDAO.updateStatus(PurchaseProcessStatus.DELETED, purchaseId, null, userId);
        } else {
            throw new InvalidDataException(invalidActionMsg);
        }
    }

    // lấy ra purchase đã save draft để sửa
    @Override
    public PurchaseRequestCreateRequest preparePurchaseRequestForm(Integer purchaseId) {

        // check tồn tại
        Purchase purchase = purchaseDAO.findById(purchaseId)
                .orElseThrow(() -> new InvalidDataException(purchaseNotFoundMsg));

        // nếu ko phải draft thì ko thể sửa
        if (PurchaseProcessStatus.DRAFT != purchase.getStatus()) {
            throw new InvalidDataException(notDraftMsg);
        }

        // trả về purchase request và toàn bộ detail đã có
        return PurchaseRequestCreateRequest.builder()
                .purchaseId(purchase.getId())
                .purchaseNote(purchase.getPurchaseNote())
                .neededByDate(purchase.getNeededByDate())
                .reason(purchase.getReason())
                .priority(purchase.getPriority())
                .purchaseRequestDetailCreateRequests(purchase.getPurchaseDetails().stream()
                        .map(pd -> PurchaseRequestDetailCreateRequest.builder()
                                .typeId(pd.getTypeId())
                                .quantity(pd.getQuantity())
                                .estimatePrice(pd.getEstimatePrice())
                                .specificationRequirement(pd.getSpecificationRequirement())
                                .purchaseDetailNote(pd.getPurchaseDetailNote())
                                .build())
                        .toList())
                .build();

    }

    private List<Purchase> author(List<Purchase> purchases){
        if (!Role.ASSET_MANAGER.name().equals(roleLogin.getRole()))
            purchases = purchases.stream()
                    .filter(p -> !PurchaseProcessStatus.DRAFT.equals(p.getStatus()))
                    .toList();


        if (Role.PURCHASE_STAFF.name().equals(roleLogin.getRole()))
            purchases = purchases.stream()
                    .filter(p -> !PurchaseProcessStatus.REJECTED.equals(p.getStatus()))
                    .toList();

        return purchases;
    }

}
