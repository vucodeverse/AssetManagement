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
import edu.fpt.groupfive.service.QuotationService;
import edu.fpt.groupfive.service.SupplierService;
import edu.fpt.groupfive.util.OrderCalculationUtil;
import edu.fpt.groupfive.util.RangeAmount;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final SupplierService supplierService;
    private final RangeAmount rangeAmount;
    private final AssetTypeService assetTypeService;
    private final OrderCalculationUtil orderCalculationUtil;


    // helper map cả supplier
    private QuotationResponse toQuotationResponse(Quotation q, Map<Integer, String> supplierMap) {
        return QuotationResponse.builder()
                .quotationId(q.getId())
                .purchaseId(q.getPurchaseId())
                .quotationStatus(q.getQuotationStatus())
                .totalAmount(q.getTotalAmount())
                .createdAt(q.getCreatedAt())
                .supplierName(supplierMap.getOrDefault(q.getSupplierId(), "Không tồn tại supplier"))
                .rejectedReason(q.getRejectedReason())
                .build();
    }

    // tạo quotation
    @Override
    public Integer createQuotation(QuotationCreateRequest quotationCreateRequest, Integer purchaseId, String action) {

        // ktra purchase có tồn tại và dc approve chưa
        purchaseDAO.findByIdAndStatus(purchaseId, "APPROVED")
                .orElseThrow(() -> new InvalidDataException("Purchase request này không tồn tại hoặc chưa được chấp " +
                        "nhận"));

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
            q.setUpdatedAt(LocalDate.now());
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
                throw new InvalidDataException("Invalid purchaseDetailId");

            // map sang quotation detail
            QuotationDetail quotationDetail = quotationDetailMapper.toQuotationDetail(qd);
            quotationDetail.setAssetTypeId(map.get(qd.getAssetTypeName()));
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
            q.setCreatedAt(LocalDate.now());

            return quotationDAO.insert(q);
        }

    }

    // lấy ra quotation đã save draft để sửa
    @Override
    public QuotationCreateRequest getQuotationRequestById(Integer id) {

        // check quotation có tồn tịa hay ko
        Quotation q = quotationDAO.findById(id)
                .orElseThrow(() -> new InvalidDataException("Quotation not found"));
        if (q.getQuotationStatus() != QuotationStatus.DRAFT) {
            throw new InvalidDataException("Chỉ có báo giá nháp mới có thể cập nhật");
        }

        // lấy ra list detail theo quotation id
        List<QuotationDetail> detailsList = quotationDetailDAO.findByQuotationId(q.getId());

        // lấy ra tên của asset type
        Map<Integer, String> assetTypeMap = assetTypeService.getAssetTypeIdToNameMap();

        List<QuotationDetailCreateRequest> quotationDetailCreateRequests = new ArrayList<>();

        // duyệt từng quotation detial để add vào list
        for (QuotationDetail qd : detailsList) {
            String assetTypeName = assetTypeMap.getOrDefault(qd.getAssetTypeId(), "Loại tài sản này không tồn tại");

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
    public void actionWithQuota(Integer id, String action,String reason) {

        quotationDAO.findById(id).orElseThrow(() -> new InvalidDataException("Quotation not found"));

        if("r".equals(action)) {
        quotationDAO.updateStatusReject(id, QuotationStatus.REJECTED, reason);
        }else if("d".equals(action)) {
            quotationDAO.updateStatusReject(id, QuotationStatus.DELETED, null);
        }

    }


    // kiểm tra form để tạo quotation
    @Override
    public QuotationCreateRequest checkFormQuotation(Integer purchaseId) {

        // load purchase request với status Approve
        Purchase purchase = purchaseDAO.findByIdAndStatus(purchaseId, Request.APPROVED.name())
                .orElseThrow(() -> new InvalidDataException("Purchase " +
                        "request này chưa được chấp nhận."));

        // load list detail nếu có
        List<PurchaseDetail> purchaseDetailList = purchaseDetailDAO.findByPurchaseRequestId(purchaseId);

        // list detail quotation
        List<QuotationDetailCreateRequest> quotationDetailCreateRequests = new ArrayList<>();

        // chuyển từ purchase detail vào quotation create
        for (PurchaseDetail purchaseDetail : purchaseDetailList) {

            String assetTypeName = purchaseDetail.getTypeId() != null
                    ? assetTypeService.findNameById(purchaseDetail.getTypeId())
                    : "Không tồn tại loại tài sản";

            QuotationDetailCreateRequest item = QuotationDetailCreateRequest.builder()
                    .purchaseRequestDetailId(purchaseDetail.getId())
                    .quantity(purchaseDetail.getQuantity())
                    .assetTypeName(assetTypeName)
                    .specificationRequirement(purchaseDetail.getSpecificationRequirement())
                    .build();

            quotationDetailCreateRequests.add(item);
        }

        // trả về quotation request
        return QuotationCreateRequest.builder()
                .purchaseId(purchaseId)
                .quotationDetailCreateRequests(quotationDetailCreateRequests)
                .build();

    }

    // hiển thị các báo giá của 1 purhase
    @Override
    public List<QuotationResponse> getQuotationsByPurchase(Integer purchaseId) {
        purchaseDAO.findById(purchaseId)
                .orElseThrow(() -> new InvalidDataException("Purchase request không tồn tại"));

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
        Quotation q = quotationDAO.findResponseById(quotationId)
                .orElseThrow(() -> new InvalidDataException("Quotation not found"));

        // lấy ra list quotation detail từ DB
        List<QuotationDetail> details = quotationDetailDAO.findByQuotationId(q.getId());

        Map<Integer, String> assetTypeMap = assetTypeService.getAssetTypeIdToNameMap();
        // map sang response cho cho detail
        List<QuotationDetailResponse> quotationDetailResponses = details.stream()
                .map(qd -> QuotationDetailResponse.builder()
                        .quotationId(q.getId())
                        .assetTypeName(assetTypeMap.getOrDefault(qd.getAssetTypeId(), "Loại tài sản này không tồn tại"))
                        .specificationRequirement(qd.getSpecificationRequirement())
                        .quantity(qd.getQuantity())
                        .quotationDetailNote(qd.getQuotationDetailNote())
                        .warrantyMonths(qd.getWarrantyMonths())
                        .price(qd.getPrice())
                        .discountRate(qd.getDiscountRate())
                        .build())
                .toList();

        // lấy tên supplier
        Map<Integer, String> supplierMap = supplierService.getSupplierIdToNameMap();
        String supplierName = supplierMap.getOrDefault(q.getSupplierId(), "Supplier ko tồn tại");

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
                .supplierName(supplierName)
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
    public List<QuotationSummaryResponse> searchAndFilterForQuotation(QuotationSearchCriteria s) {

        // ktra from và to có khớp ko
        if (s.getFrom() != null && s.getTo() != null && s.getFrom().isAfter(s.getTo())) {
            throw new InvalidDataException("From phải trước To");
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
        Map<Integer, Object[]> summaryMap = purchaseDAO.findQuotaSummaryByFilter(s);

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
        return searchAndFilterForQuotation(new QuotationSearchCriteria());
    }

    // method search cho màn qop
    @Override
    public List<QuotationResponse> quotationCriteriaForPurchase(QuotationSearchCriteria criteria) {

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
        List<Quotation> quotations = quotationDAO.searchAndFilterQuotationOfPurchase(criteria);

        // lấy toàn bọ supplier
        Map<Integer, String> supplierMap = supplierService.getSupplierIdToNameMap();

        // map về quotation response
        return quotations.stream()
                .map(q -> toQuotationResponse(q, supplierMap))
                .toList();
    }

    // chuyển từ purchase detail sang quotation detail để tọa form nhập
    @Override
    public List<QuotationDetailCreateRequest> mapPurchaseToQuotation(Integer purchaseId) {

        // check xem purrchase đã đc approve chưa
        purchaseDAO.findByIdAndStatus(purchaseId, Request.APPROVED.name()).orElseThrow(() -> new InvalidDataException(
                "Yêu cầu mua sắm chưa đc cấp nhận"));

        // lấy ra list purchase detail
        List<PurchaseDetail> prDetails = purchaseDetailDAO.findByPurchaseRequestId(purchaseId);

        // lấy ra asset type kèm tên
        Map<Integer, String> map = assetTypeService.getAssetTypeIdToNameMap();

        // thực hiện map sang quotation detail
        return prDetails.stream()
                .map(pd -> {
                    String assetTypeName = map.getOrDefault(pd.getTypeId(), "Không tồn tại loại tài sản này");

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

}
