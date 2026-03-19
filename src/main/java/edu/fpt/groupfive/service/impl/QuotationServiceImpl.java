package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.QuotationStatus;
import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dao.*;
import edu.fpt.groupfive.dto.request.QuotationDetailCreateRequest;
import edu.fpt.groupfive.dto.request.QuotationCreateRequest;
import edu.fpt.groupfive.dto.request.QuotationSearchCriteria;
import edu.fpt.groupfive.dto.response.QuotationDetailResponse;
import edu.fpt.groupfive.dto.response.QuotationSummaryResponse;
import edu.fpt.groupfive.dto.response.QuotationResponse;
import edu.fpt.groupfive.mapper.QuotationDetailMapper;
import edu.fpt.groupfive.mapper.QuotationMapper;
import edu.fpt.groupfive.model.*;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.ISupplierService;
import edu.fpt.groupfive.service.QuotationService;
import edu.fpt.groupfive.util.OrderCalculationUtil;
import edu.fpt.groupfive.util.RangeAmount;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
                .rejectedReason(q.getRejectedReason())
                .build();
    }

    // tạo quotation
    @Override
    public Integer createQuotation(QuotationCreateRequest quotationCreateRequest, Integer purchaseId, String action) {

        // ktra purchase có tồn tại và dc approve chưa
        purchaseDAO.findByIdAndStatus(purchaseId, "APPROVED")
                .orElseThrow(() -> new InvalidDataException(purchaseNotApprovedMsg));

        // lấy ra list purchase detail của purchase request nhận vào
        List<PurchaseDetail> details = purchaseDetailDAO.findByPurchaseRequestId(purchaseId);

        // Sử dụng Map để kiểm tra sự tồn tại của chi tiết yêu cầu mua sắm
        Map<Integer, PurchaseDetail> purchaseDetailMap = new HashMap<>();

        // key là ID,value là detail
        for (PurchaseDetail d : details) {
            purchaseDetailMap.put(d.getId(), d);
        }

        // map quotation create về quotation
        Quotation q = quotationMapper.toQuotation(quotationCreateRequest);
        q.setPurchaseId(purchaseId);

        // kiểm tra xem save hay draft
        QuotationStatus quotationStatus = "draft".equalsIgnoreCase(action) ? QuotationStatus.DRAFT
                : QuotationStatus.PENDING;

        // set các giá trị
        q.setTotalAmount(orderCalculationUtil.calculateTotal(quotationCreateRequest));

        // nếu là draft thì update thời gian sửa
        if (QuotationStatus.DRAFT.equals(quotationStatus)) {
            q.setUpdatedAt(LocalDateTime.now());
        }

        // map ngược lại
        Map<String, Integer> map = new HashMap<>();

        for (Map.Entry<Integer, String> e : assetTypeService.getAssetTypeIdToNameMap().entrySet()) {
            map.put(e.getValue(), e.getKey());
        }

        // Khởi tạo danh sách chi tiết báo giá trước
        List<QuotationDetail> quotationDetails = new ArrayList<>();

        BigDecimal totalAmount = BigDecimal.ZERO;

        // mapừng quotation detail
        for (QuotationDetailCreateRequest qd : quotationCreateRequest.getQuotationDetailCreateRequests()) {

            // ktra purchase detail có tồn tại hay ko
            PurchaseDetail pd = purchaseDetailMap.get(qd.getPurchaseRequestDetailId());
            if (pd == null)
                throw new InvalidDataException(invalidDetailIdMsg);

            // map sang quotation detail
            QuotationDetail quotationDetail = quotationDetailMapper.toQuotationDetail(qd);
            quotationDetail.setAssetTypeId(map.get(qd.getAssetTypeName()));
            quotationDetail.setQuotationDetailStatus(QuotationStatus.PENDING);
            quotationDetails.add(quotationDetail);
        }

        q.setQuotationDetails(quotationDetails);

        // check xem quotation này đã được tạo hay chưa
        if (quotationCreateRequest.getQuotationId() != null) {
            q.setId(quotationCreateRequest.getQuotationId());
            q.setQuotationStatus(quotationStatus);
            quotationDAO.update(q);

            return quotationCreateRequest.getQuotationId();
        } else {
            q.setQuotationStatus(quotationStatus);
            q.setCreatedAt(LocalDateTime.now());

            return quotationDAO.insert(q);
        }
    }

    // lấy ra quotation đã save draft để sửa
    @Override
    public QuotationCreateRequest prepareQuotationUpdateForm(Integer id) {

        // check quotation có tồn tịa hay ko
        Quotation q = quotationDAO.findById(id)
                .orElseThrow(() -> new InvalidDataException(quotationNotFoundMsg));
        if (q.getQuotationStatus() != QuotationStatus.DRAFT) {
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
    public void processQuotationAction(Integer id, String action, String reason) {

       Quotation quotation =
               quotationDAO.findById(id).orElseThrow(() -> new InvalidDataException(quotationNotFoundMsg));

       List<QuotationDetail>  detailsList =
               quotationDetailDAO.findByQuotationId(quotation.getId()).stream().filter(qd -> QuotationStatus.REJECTED != qd.getQuotationDetailStatus() && QuotationStatus.DELETED != qd.getQuotationDetailStatus()).toList();

        if ("r".equals(action)) {
            for(int i = 0; i< detailsList.size(); i++){
                quotationDetailDAO.update(detailsList.get(i).getId(), QuotationStatus.REJECTED);
            }
            quotationDAO.updateStatus(id, QuotationStatus.REJECTED, reason);
        } else if ("d".equals(action)) {

            for(int i = 0; i< detailsList.size(); i++){
                quotationDetailDAO.update(detailsList.get(i).getId(), QuotationStatus.DELETED);
            }
            quotationDAO.updateStatus(id, QuotationStatus.DELETED, null);
        }

    }


    // hiển thị các báo giá của 1 purhase
    @Override
    public List<QuotationResponse> getQuotationsByPurchaseId(Integer purchaseId) {
        purchaseDAO.findById(purchaseId)
                .orElseThrow(() -> new InvalidDataException(prNotFoundMsg));

        // lấy ra supplier
        Map<Integer, String> supplierMap = supplierService.getSupplierIdToNameMap();

        // map về response
        return quotationDAO.findByPurchaseId(purchaseId).stream()
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
        List<QuotationDetail> details =
                quotationDetailDAO.findByQuotationId(q.getId()).stream().filter(qd -> QuotationStatus.DELETED != qd.getQuotationDetailStatus()).toList();

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
                        .discountRate(qd.getDiscountRate())
                        .status(qd.getQuotationDetailStatus())
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
                .rejectedReason(q.getRejectedReason())
                .build();
    }

    // thực hiện search và filter cho màn quotation list
    @Override
    public List<QuotationSummaryResponse> searchQuotations(QuotationSearchCriteria s) {

        // ktra from và to có khớp ko
        if (s.getFrom() != null && s.getTo() != null && s.getFrom().isAfter(s.getTo())) {
            throw new InvalidDataException(searchInvalidDateMsg);
        }

        // mặc định null
        s.setMinAmount(null);
        s.setMaxAmount(null);

        if (s.getAmountRange() != null && !s.getAmountRange().isBlank()) {

            // tách min và max amount
            List<BigDecimal> list = rangeAmount.applyRangeAMount(s.getAmountRange());
            if (list.size() == 1) {
                s.setMinAmount(list.get(0));
            } else if (list.size() == 2) {
                s.setMinAmount(list.get(0));
                s.setMaxAmount(list.get(1));
            }
        }

        // Khóa: ID yêu cầu mua sắm - Giá trị: mảng các đối tượng chứa thông tin tóm tắt
        // của quotaiton
        Map<Integer, Object[]> summaryMap = purchaseDAO.searchQuotationSummary(s);

        List<QuotationSummaryResponse> out = new ArrayList<>();
        for (Map.Entry<Integer, Object[]> entry : summaryMap.entrySet()) {
            Object[] data = entry.getValue();

            // trả về list QuotationForPurchase
            out.add(QuotationSummaryResponse.builder()
                    .purchaseId(entry.getKey())
                    .needByDate((LocalDate) data[0])
                    .priority((String) data[1])
                    .numberOfQuotation((Integer) data[2])
                    .estPrice((BigDecimal) data[3])
                    .build());
        }
        return out;
    }

    // mặc định của màn quotation list
    @Override
    public List<QuotationSummaryResponse> getQuotationAndPurchase() {
        return searchQuotations(new QuotationSearchCriteria());
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
        purchaseDAO.findByIdAndStatus(purchaseId, Request.APPROVED.name()).orElseThrow(() -> new InvalidDataException(
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
    public void processQuotationDetailAction(Integer id, String actions) {
        quotationDetailDAO.findById(id).orElseThrow(() -> new InvalidDataException("Không tồn tại báo giá này"));

        if("a".equals(actions)){
            quotationDetailDAO.update(id, QuotationStatus.APPROVED);
        }else if("r".equals(actions)){
            quotationDetailDAO.update(id, QuotationStatus.REJECTED);
        }
    }

}
