package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.Request;
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
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    // tạo 1 purchase request
    @Override
    public Integer createPurchaseRequest(PurchaseRequestCreateRequest purchaseCreateRequest, int userId,
            Request request) {

        // map từ dto sang purchase
        Purchase purchase = purchaseMapper.toPurchase(purchaseCreateRequest);

        // set status
        purchase.setStatus(request);

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
    public PurchaseRequestResponse findById(Integer id) {

        Map<Integer, String> userMap = userService.getUserIdToUsernameMap();
        Map<Integer, String> assetTypeMap = assetTypeService.getAssetTypeIdToNameMap();

        return purchaseDAO.findById(id)
                .map(p -> {
                    // map purchase details
                    List<PurchaseRequestDetailResponse> details = p.getPurchaseDetails().stream()
                            .map(pd -> PurchaseRequestDetailResponse.builder()
                                    .id(pd.getId())
                                    .assetTypeName(assetTypeMap.getOrDefault(pd.getTypeId(),
                                            "Không tồn tại loại tài sản"))
                                    .quantity(pd.getQuantity())
                                    .estimatePrice(pd.getEstimatePrice())
                                    .specificationRequirement(pd.getSpecificationRequirement())
                                    .purchaseDetailNote(pd.getPurchaseDetailNote())
                                    .build())
                            .toList();

                    // tính total amount
                    BigDecimal totalAmount = p.getPurchaseDetails().stream().map(pd ->
                         pd.getEstimatePrice().multiply(new BigDecimal(pd.getQuantity()))).reduce(BigDecimal.ZERO, BigDecimal::add);

                    // map snag purchase response
                    PurchaseRequestResponse resp = purchaseMapper.toPurchaseResponse(p);
                    resp.setCreatorName(userMap.getOrDefault(p.getCreatedByUser(), "Không tồn tại người dùng"));
                    resp.setPurchaseDetails(details);
                    resp.setQuotationCount(quotationDAO.countQuotationFromPurchaseId(p.getId()));
                    resp.setTotalAmount(totalAmount);
                    return resp;
                })
                .orElseThrow(() -> new InvalidDataException("Purchase request không tồn tại: " + id));
    }

    // lấy ra tấy cả purchase
    @Override
    public List<PurchaseRequestResponse> findAllPurchases() {

        // lấy ra tất first name và last name của use
        Map<Integer, String> userMap = userService.getUserIdToUsernameMap();

        return purchaseDAO.findAll().stream().map(p -> {

            // map sang response để trả về client
            PurchaseRequestResponse resp = purchaseMapper.toPurchaseResponse(p);

            // lấy ra tên người dùng
            resp.setCreatorName(userMap.getOrDefault(p.getCreatedByUser(), "Không tồn tại người dùng"));
            return resp;
        }).toList();
    }

    // thực hiện search và filter
    @Override
    public List<PurchaseRequestResponse> searchAndFilter(PurchaseRequestSearchCriteria p) {

        // validate ngày tháng nhập vào có hợp lí ko
        if (p.getFrom() != null && p.getTo() != null
                && p.getFrom().isAfter(p.getTo())) {
            throw new InvalidDataException("From phải trước To");
        }

        // lấy ra user
        Map<Integer, String> userMap = userService.getUserIdToUsernameMap();

        return purchaseDAO.getPurchaseByFilter(p).stream()
                .map(pr -> {

                    // Chuyển đổi sang PurchaseRequestResponse để hiển thị trên giao diện
                    PurchaseRequestResponse resp = purchaseMapper.toPurchaseResponse(pr);
                    resp.setCreatorName(userMap.getOrDefault(pr.getCreatedByUser(), "Không tồn tại người dùng"));
                    return resp;
                })
                .toList();
    }

    // thực hiện save và draft
    @Override
    public void actionsWithPurchase(Integer purchaseId, String action, String reasonReject, Integer userId) {

        // check purchase có tồn tại hay ko
        purchaseDAO.findById(purchaseId).orElseThrow(() -> new InvalidDataException("Purchase request không tộn tại"));

        // Xử lý các hành động (actions) nhận được từ controller
        if ("a".equals(action)) {
            purchaseDAO.updatePurchaseStatus(Request.APPROVED, purchaseId, null, userId);
        } else if ("r".equals(action)) {
            purchaseDAO.updatePurchaseStatus(Request.REJECTED, purchaseId, reasonReject, userId);
        } else if ("d".equals(action)) {
            purchaseDAO.updatePurchaseStatus(Request.DELETED, purchaseId, null, userId);
        } else {
            throw new InvalidDataException("Action không hợp lệ: " + action);
        }
    }

    // lấy ra purchase đã save draft để sửa
    @Override
    public PurchaseRequestCreateRequest loadDraftForEdit(Integer purchaseId) {

        // check tồn tại
        Purchase purchase = purchaseDAO.findById(purchaseId)
                .orElseThrow(() -> new InvalidDataException("Purchase request không tồn tại: " + purchaseId));

        // nếu ko phải draft thì ko thể sửa
        if (Request.DRAFT != purchase.getStatus()) {
            throw new InvalidDataException("Yêu cầu mua sắm này không thể update");
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
}
