package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.PurchaseProcessStatus;

import edu.fpt.groupfive.common.Role;
import edu.fpt.groupfive.dao.*;
import edu.fpt.groupfive.dto.request.QuotationDetailCreateRequest;
import edu.fpt.groupfive.dto.request.QuotationCreateRequest;
import edu.fpt.groupfive.dto.request.QuotationSearchCriteria;
import edu.fpt.groupfive.dto.response.QuotationDetailResponse;
import edu.fpt.groupfive.dto.response.QuotationResponse;
import edu.fpt.groupfive.mapper.QuotationDetailMapper;
import edu.fpt.groupfive.mapper.QuotationMapper;
import edu.fpt.groupfive.model.*;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.ISupplierService;
import edu.fpt.groupfive.service.QuotationService;
import edu.fpt.groupfive.util.OrderCalculationUtil;
import edu.fpt.groupfive.util.RangeAmount;
import edu.fpt.groupfive.util.config.RoleLogin;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuotationServiceImpl implements QuotationService {

    private final QuotationDAO quotationDAO;
    private final QuotationDetailDAO quotationDetailDAO;
    private final PurchaseDAO purchaseDAO;
    private final PurchaseDetailDAO purchaseDetailDAO;
    private final QuotationMapper quotationMapper;
    private final QuotationDetailMapper quotationDetailMapper;
    private final ISupplierService supplierService;
    private final RangeAmount rangeAmount;
    private final AssetTypeService assetTypeService;
    private final OrderCalculationUtil orderCalculationUtil;
    private final RoleLogin roleLogin;

    @Value("${quotation.supplier_not_found}")
    private String supplierNotFoundMsg;

    @Value("${quotation.create.purchase_not_approved}")
    private String purchaseNotApprovedMsg;

    @Value("${quotation.create.invalid_detail_id}")
    private String invalidDetailIdMsg;

    @Value("${quotation.not_found}")
    private String quotationNotFoundMsg;

    @Value("${quotation.edit.not_draft}")
    private String editNotDraftMsg;

    @Value("${quotation.asset_type_not_found}")
    private String assetTypeNotFoundMsg;
    @Value("${quotation.detail.not_found}")
    private String quotationDetailNotFoundMsg;

    @Value("${quotation.pr_not_approved}")
    private String prNotApprovedMsg;

    @Value("${quotation.pr_not_found}")
    private String prNotFoundMsg;

    @Value("${quotation.supplier_not_found_alt}")
    private String supplierNotFoundAltMsg;

    @Value("${quotation.search.invalid_date}")
    private String searchInvalidDateMsg;

    @Value("${quotation.map.not_approved}")
    private String mapNotApprovedMsg;

    @Value("${quotation.detail.asset_type_not_found}")
    private String detailAssetTypeNotFoundMsg;

    @Value("${purchase.detail.asset_type_not_found}")
    private String purchaseAssetTypeNotFoundMsg;

    // helper map cả supplier
    private QuotationResponse toQuotationResponse(Quotation q, Map<Integer, String> supplierMap) {
        return QuotationResponse.builder()
                .quotationId(q.getId())
                .purchaseId(q.getPurchaseId())
                .quotationStatus(q.getQuotationStatus())
                .totalAmount(q.getTotalAmount())
                .createdAt(q.getCreatedAt())
                .supplierName(supplierMap.getOrDefault(q.getSupplierId(), supplierNotFoundMsg))
                .build();
    }

    // tạo quotation
    @Override
    public Integer createQuotation(QuotationCreateRequest quotationCreateRequest, Integer purchaseId, String action) {

        // valid dữ liệu đầu vào
        Purchase purrchase =
                purchaseDAO.findByIdAndStatus(purchaseId, PurchaseProcessStatus.APPROVED.name()).orElseThrow(() -> new InvalidDataException(purchaseNotApprovedMsg));

        //  lấy dữ liệu id và pr detail của nó
        Map<Integer, PurchaseDetail> purchaseDetailMap =
                purchaseDetailDAO.findByPurchaseRequestId(purchaseId).stream().collect(Collectors.toMap(PurchaseDetail::getId,  purchaseDetail -> purchaseDetail));

        Map<String, Integer> assetTypeNameToIdMap = assetTypeService.getNameToIdMap();

        Quotation quotation = quotationMapper.toQuotation(quotationCreateRequest);
        quotation.setPurchaseId(purchaseId);

        PurchaseProcessStatus status = "draft".equals(action) ? PurchaseProcessStatus.DRAFT :
                PurchaseProcessStatus.PENDING;

        quotation.setQuotationStatus(status);
        quotation.setTotalAmount(orderCalculationUtil.calculateTotal(quotationCreateRequest));

        List<QuotationDetail> details = quotationCreateRequest.getQuotationDetailCreateRequests().stream().map(
                qd -> {
                    if(!purchaseDetailMap.containsKey(qd.getPurchaseRequestDetailId())) throw new InvalidDataException(invalidDetailIdMsg);

                    QuotationDetail detail = quotationDetailMapper.toQuotationDetail(qd);
                    if(PurchaseProcessStatus.DRAFT.equals(status)) detail.setQuotationDetailStatus(PurchaseProcessStatus.DRAFT);
                    else detail.setQuotationDetailStatus(PurchaseProcessStatus.PENDING);
                    detail.setAssetTypeId(assetTypeNameToIdMap.get(qd.getAssetTypeName()));

                    return detail;
                }).toList();


        quotation.setQuotationDetails(details);

        // check xem ây có phải lần đầu tạo hay ko
        if(quotationCreateRequest.getQuotationId() != null){

            quotationDAO.findById(quotationCreateRequest.getQuotationId())
                    .orElseThrow(() -> new InvalidDataException(quotationNotFoundMsg));
            // nếu tồn tại chứng tỏ lần này là update
            quotation.setUpdatedAt(LocalDateTime.now());
            quotation.setId(quotationCreateRequest.getQuotationId());
            quotationDAO.update(quotation);
            return quotation.getId();
        }else{
            quotation.setCreatedAt(LocalDateTime.now());
            return quotationDAO.insert(quotation);

        }
    }

    // lấy ra quotation đã save draft để sửa
    @Override
    public QuotationCreateRequest prepareQuotationUpdateForm(Integer id) {

        // check quotation có tồn tịa hay ko
        Quotation q = quotationDAO.findById(id)
                .orElseThrow(() -> new InvalidDataException(quotationNotFoundMsg));
        if (q.getQuotationStatus() != PurchaseProcessStatus.DRAFT) {
            throw new InvalidDataException(editNotDraftMsg);
        }

        // lấy ra list detail theo quotation id
        List<QuotationDetail> detailsList = quotationDetailDAO.findByQuotationId(q.getId());

        // lấy ra tên của asset type
        Map<Integer, String> assetTypeMap = assetTypeService.getAssetTypeIdToNameMap();

        List<QuotationDetailCreateRequest> quotationDetailCreateRequests = new ArrayList<>();

        // duyệt từng quotation detial để add vào list
        for (QuotationDetail qd : detailsList) {
            String assetTypeName = assetTypeMap.getOrDefault(qd.getAssetTypeId(), assetTypeNotFoundMsg);

            quotationDetailCreateRequests.add(QuotationDetailCreateRequest.builder()
                    .purchaseRequestDetailId(qd.getPurchaseDetailId())
                    .assetTypeName(assetTypeName)
                    .specificationRequirement(qd.getSpecificationRequirement())
                    .quantity(qd.getQuantity())
                    .quotationDetailNote(qd.getQuotationDetailNote())
                    .warrantyMonths(qd.getWarrantyMonths())
                    .price(qd.getPrice())
                    .taxRate(qd.getTaxRate())
                    .discountRate(qd.getDiscountRate())
                    .build());
        }

        // trả về qutotaion
        return QuotationCreateRequest.builder()
                .quotationId(q.getId())
                .purchaseId(q.getPurchaseId())
                .supplierId(q.getSupplierId())
                .quotationNote(q.getQuotationNote())
                .quotationDetailCreateRequests(quotationDetailCreateRequests)
                .build();
    }

    // update quotation
    @Override
    public void processQuotationAction(Integer id, String action) {

        Quotation quotation = quotationDAO.findById(id)
                .orElseThrow(() -> new InvalidDataException(quotationNotFoundMsg));

        List<QuotationDetail> detailsList = quotationDetailDAO.findByQuotationId(quotation.getId()).stream()
                .filter(qd -> PurchaseProcessStatus.REJECTED != qd.getQuotationDetailStatus()
                        && PurchaseProcessStatus.DELETED != qd.getQuotationDetailStatus())
                .toList();

        if ("r".equals(action)) {
            detailsList.forEach(qd -> quotationDetailDAO.update(qd.getId(), PurchaseProcessStatus.REJECTED));
            quotationDAO.updateStatus(id, PurchaseProcessStatus.REJECTED);
        } else if ("d".equals(action)) {
            detailsList.forEach(qd -> quotationDetailDAO.update(qd.getId(), PurchaseProcessStatus.DELETED));
            quotationDAO.updateStatus(id, PurchaseProcessStatus.DELETED);
        }

    }

    // hiển thị các báo giá của 1 purhase
    @Override
    public List<QuotationResponse> getQuotationsByPurchaseId(Integer purchaseId) {
        purchaseDAO.findById(purchaseId)
                .orElseThrow(() -> new InvalidDataException(prNotFoundMsg));

        // lấy ra supplier
        Map<Integer, String> supplierMap = supplierService.getSupplierIdToNameMap();
        List<Quotation> quotations = quotationDAO.findByPurchaseId(purchaseId);


        quotations = author(quotations);

        // map về response
        return quotations.stream()
                .map(q -> toQuotationResponse(q, supplierMap))
                .toList();
    }

    // lấy ra quotation theo id
    @Override
    public QuotationResponse getQuotationById(Integer quotationId) {

        // ktra quotaiton có tồn tại hay ko
        Quotation q = quotationDAO.findById(quotationId)
                .orElseThrow(() -> new InvalidDataException(quotationNotFoundMsg));

        // lấy ra list quotation detail từ DB
        List<QuotationDetail> details = quotationDetailDAO.findByQuotationId(q.getId()).stream()
                .filter(qd -> PurchaseProcessStatus.DELETED != qd.getQuotationDetailStatus()).toList();

        // lấy ra các loại tài sản
        Map<Integer, String> assetTypeMap = assetTypeService.getAssetTypeIdToNameMap();

        // map sang response cho cho detail
        List<QuotationDetailResponse> quotationDetailResponses = details.stream()
                .map(qd -> QuotationDetailResponse.builder()
                        .quotationId(q.getId())
                        .quotationDetailId(qd.getId())
                        .assetTypeName(qd.getAssetTypeId() == null ? assetTypeNotFoundMsg
                                : assetTypeMap.getOrDefault(qd.getAssetTypeId(), assetTypeNotFoundMsg))
                        .specificationRequirement(qd.getSpecificationRequirement())
                        .quantity(qd.getQuantity())
                        .quotationDetailNote(qd.getQuotationDetailNote())
                        .warrantyMonths(qd.getWarrantyMonths())
                        .price(qd.getPrice())
                        .taxRate(qd.getTaxRate())
                        .discountRate(qd.getDiscountRate())
                        .status(qd.getQuotationDetailStatus())
                        .purchaseDetailId(qd.getPurchaseDetailId())
                        .assetTypeId(qd.getAssetTypeId())
                        .build())
                .toList();

        // lấy tên supplier
        Map<Integer, String> supplierMap = supplierService.getSupplierIdToNameMap();

        // tính toán chi tiết từng đầu giá của quotation
        BigDecimal[] calculated = orderCalculationUtil.calculateQuotationPrice(details);
        BigDecimal subtotal = calculated[0];
        BigDecimal totalDiscount = calculated[1];
        BigDecimal totalTax = calculated[2];
        BigDecimal grandTotal = calculated[3];

        return QuotationResponse.builder()
                .quotationId(q.getId())
                .purchaseId(q.getPurchaseId())
                .quotationStatus(q.getQuotationStatus())
                .totalAmount(q.getTotalAmount())
                .createdAt(q.getCreatedAt())
                .supplierName(supplierMap.getOrDefault(q.getSupplierId(), supplierNotFoundAltMsg))
                .subtotal(subtotal)
                .totalDiscount(totalDiscount)
                .totalTax(totalTax)
                .grandTotal(grandTotal)
                .quotationDetails(quotationDetailResponses)
                .build();
    }

    // method search cho màn qop
    @Override
    public List<QuotationResponse> searchQuotationsByPurchaseId(QuotationSearchCriteria criteria) {

        // set mặc định trước tránh lặp lại giá trị cũ
        criteria.setMinAmount(null);
        criteria.setMaxAmount(null);

        if (criteria.getAmountRange() != null && !criteria.getAmountRange().isBlank()) {

            // tách giá trị range amout
            List<BigDecimal> list = rangeAmount.applyRangeAMount(criteria.getAmountRange());
            if (list.size() == 1) {
                criteria.setMinAmount(list.get(0));
            } else if (list.size() == 2) {
                criteria.setMinAmount(list.get(0));
                criteria.setMaxAmount(list.get(1));
            }
        }

        // lấy ra list quotation sau khí search
        List<Quotation> quotations = quotationDAO.searchByPurchaseId(criteria);

        // lấy toàn bọ supplier
        Map<Integer, String> supplierMap = supplierService.getSupplierIdToNameMap();

        // map về quotation response
        return quotations.stream()
                .map(q -> toQuotationResponse(q, supplierMap))
                .toList();
    }

    // chuyển từ purchase detail sang quotation detail để tọa form nhập
    @Override
    public List<QuotationDetailCreateRequest> prepareQuotationForm(Integer purchaseId) {

        // check xem purrchase đã đc approve chưa
        purchaseDAO.findByIdAndStatus(purchaseId, PurchaseProcessStatus.APPROVED.name()).orElseThrow(() -> new InvalidDataException(
                mapNotApprovedMsg));

        // lấy ra list purchase detail
        List<PurchaseDetail> prDetails = purchaseDetailDAO.findByPurchaseRequestId(purchaseId);

        // lấy ra asset type kèm tên
        Map<Integer, String> map = assetTypeService.getAssetTypeIdToNameMap();

        // thực hiện map sang quotation detail
        return prDetails.stream()
                .map(pd -> {
                    String assetTypeName = map.getOrDefault(pd.getTypeId(), detailAssetTypeNotFoundMsg);

                    return QuotationDetailCreateRequest.builder()
                            .purchaseRequestDetailId(pd.getId())
                            .assetTypeName(assetTypeName)
                            .specificationRequirement(pd.getSpecificationRequirement())
                            .quantity(pd.getQuantity())
                            .price(BigDecimal.ZERO)
                            .taxRate(BigDecimal.TEN)
                            .discountRate(BigDecimal.ZERO)
                            .quotationDetailNote(pd.getPurchaseDetailNote())
                            .build();
                })
                .toList();
    }


    @Override
    public void processQuotationDetailAction(Integer id, String actions, Integer qoId) {
        quotationDetailDAO.findById(id).orElseThrow(() -> new InvalidDataException(quotationDetailNotFoundMsg));

        if ("a".equals(actions)) {
            quotationDetailDAO.update(id, PurchaseProcessStatus.APPROVED);
            quotationDAO.updateUpdatedAt(qoId);
        } else if ("r".equals(actions)) {
            quotationDetailDAO.update(id, PurchaseProcessStatus.REJECTED);
            quotationDAO.updateUpdatedAt(qoId);
            List<QuotationDetail> quotationDetails = quotationDetailDAO.findByQuotationId(qoId);

            boolean isRejectAll =
                    quotationDetails.stream().allMatch(qd -> PurchaseProcessStatus.REJECTED.equals(qd.getQuotationDetailStatus()));


            if(isRejectAll) {
                quotationDAO.updateStatus(qoId, PurchaseProcessStatus.REJECTED);
            }
        }
    }

    private List<Quotation> author(List<Quotation> quotations){
        List<PurchaseProcessStatus> excludedStatuses = List.of(
                PurchaseProcessStatus.DRAFT,
                PurchaseProcessStatus.DELETED
        );

        if (!Role.PURCHASE_STAFF.name().equals(roleLogin.getRole()))
            quotations = quotations.stream().filter(q -> !excludedStatuses.contains(q.getQuotationStatus()))
                    .toList();


        return quotations;
    }

}
