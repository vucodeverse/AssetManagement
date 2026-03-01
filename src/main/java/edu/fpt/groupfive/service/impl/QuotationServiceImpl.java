package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.QuotationStatus;
import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dao.*;
import edu.fpt.groupfive.dto.request.QuotationCreateDetailRequest;
import edu.fpt.groupfive.dto.request.QuotationCreateRequest;
import edu.fpt.groupfive.dto.request.QuotationSearchCriteria;
import edu.fpt.groupfive.dto.request.SearchForQuotation;
import edu.fpt.groupfive.dto.response.QuotationDetailResponse;
import edu.fpt.groupfive.dto.response.QuotationForPurchaseResponse;
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

    // ===== Hàm hỗ trợ: dùng chung cho getQuotationsByPurchase +
    // quotationCriteriaForPurchase =====
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

    @Override
    public void createQuotation(QuotationCreateRequest quotationCreateRequest, Integer purchaseId, String action) {

        // ktra purchase có tồn tại và dc approve chưa
        purchaseDAO.findByIdAndApproved(purchaseId, "APPROVED")
                .orElseThrow(() -> new InvalidDataException("Purchase request này không tồn tại hoặc chưa được chấp " +
                        "nhận"));

        // lấy ra list purchase detail của purchase request nhận vào
        List<PurchaseDetail> details = purchaseDetailDAO.findByPurchaseRequestId(purchaseId);

        // Sử dụng Map để kiểm tra sự tồn tại của chi tiết yêu cầu mua sắm (purchase
        // detail)
        Map<Integer, PurchaseDetail> purchaseDetailMap = new HashMap<>();

        // Khóa là ID, giá trị là chi tiết (detail)
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
        if (QuotationStatus.DRAFT.equals(quotationStatus)) {
            q.setUpdatedAt(LocalDate.now());
        }

        // Khởi tạo danh sách chi tiết báo giá trước
        List<QuotationDetail> quotationDetails = new ArrayList<>();
        for (QuotationCreateDetailRequest qd : quotationCreateRequest.getQuotationCreateDetailRequestList()) {

            // ktra purchase detail có tồn tại hay ko
            PurchaseDetail pd = purchaseDetailMap.get(qd.getPurchaseRequestDetailId());
            if (pd == null)
                throw new InvalidDataException("Invalid purchaseDetailId");

            // map sang quotation detail
            QuotationDetail quotationDetail = quotationDetailMapper.toQuotationDetail(qd);
            quotationDetail.setPurchaseDetailId(pd.getId());
            if (pd.getAssetTypeId() != null) {
                quotationDetail.setAssetTypeId(pd.getAssetTypeId());
            }
            if (quotationDetail.getSpecificationRequirement() == null && pd.getSpecificationRequirement() != null) {
                quotationDetail.setSpecificationRequirement(pd.getSpecificationRequirement());
            }
            quotationDetails.add(quotationDetail);
        }

        // Gán danh sách chi tiết vào báo giá. DAO sẽ thực hiện thêm/sửa tất cả trong
        // một giao dịch (transaction)
        q.setQuotationDetails(quotationDetails);

        // check xem quotation này đã được tạo hay chưa
        if (quotationCreateRequest.getQuotationId() != null) {
            q.setId(quotationCreateRequest.getQuotationId());
            q.setQuotationStatus(quotationStatus);
            quotationDAO.update(q);
        } else {
            q.setQuotationStatus(quotationStatus);
            q.setCreatedAt(LocalDate.now());
            quotationDAO.insert(q);
        }
    }

    // lấy ra quotation đã save draft để sửa
    @Override
    public QuotationCreateRequest getQuotationRequestById(Integer id) {
        Quotation q = quotationDAO.findById(id)
                .orElseThrow(() -> new InvalidDataException("Quotation not found"));

        // map quotation detail sang response
        List<QuotationDetailResponse> details = quotationDetailDAO.findByQuotationId(q.getId()).stream()
                .map(quotationDetailMapper::toQuotationDetailResponse).toList();

        List<QuotationCreateDetailRequest> quotationCreateDetailRequestList = new ArrayList<>();

        // Duyệt qua từng chi tiết báo giá
        for (QuotationDetailResponse d : details) {

            // add quotaiton detail đã dc tạo vào quotaiton create list
            quotationCreateDetailRequestList.add(QuotationCreateDetailRequest.builder()
                    .purchaseRequestDetailId(d.getPurchaseDetailId())
                    .assetTypeName(d.getAssetTypeName())
                    .specificationRequirement(d.getSpecificationRequirement())
                    .quantity(d.getQuantity())
                    .quotationDetailNote(d.getQuotationDetailNote())
                    .warrantyMonths(d.getWarrantyMonths())
                    .price(d.getPrice())
                    .taxRate(d.getTaxRate())
                    .discountRate(d.getDiscountRate())
                    .build());
        }

        // trả về qutotaion
        return QuotationCreateRequest.builder()
                .quotationId(q.getId())
                .purchaseRequestId(q.getPurchaseId())
                .supplierId(q.getSupplierId())
                .quotationNote(q.getQuotationDetailNote())
                .quotationCreateDetailRequestList(quotationCreateDetailRequestList)
                .build();
    }

    // update quotation
    @Override
    public void rejectQuotation(Integer id, String reason) {
        quotationDAO.updateStatusReject(id, QuotationStatus.REJECTED, reason);
    }

    @Override
    public QuotationCreateRequest checkFormQuotation(Integer purchaseId) {

        // load purchase request với status Approve
        Purchase purchase = purchaseDAO.findByIdAndApproved(purchaseId, Request.APPROVED.name())
                .orElseThrow(() -> new InvalidDataException("Purchase " +
                        "request này chưa được chấp nhận."));

        // load list detail nếu có
        List<PurchaseDetail> purchaseDetailList = purchaseDetailDAO.findByPurchaseRequestId(purchaseId);

        // list detail quotation
        List<QuotationCreateDetailRequest> quotationCreateDetailRequestList = new ArrayList<>();

        // chuyển từ purchase detail vào quotation create
        for (PurchaseDetail purchaseDetail : purchaseDetailList) {

            // dùng assetTypeService.findNameById() từ team thay vì tự map
            String assetTypeName = purchaseDetail.getAssetTypeId() != null
                    ? assetTypeService.findNameById(purchaseDetail.getAssetTypeId())
                    : "Không tồn tại loại tài sản";

            QuotationCreateDetailRequest item = QuotationCreateDetailRequest.builder()
                    .purchaseRequestDetailId(purchaseDetail.getId())
                    .quantity(purchaseDetail.getQuantity())
                    .assetTypeName(assetTypeName)
                    .specificationRequirement(purchaseDetail.getSpecificationRequirement())
                    .build();

            quotationCreateDetailRequestList.add(item);
        }

        // trả về quotation request
        return QuotationCreateRequest.builder()
                .purchaseRequestId(purchaseId)
                .quotationCreateDetailRequestList(quotationCreateDetailRequestList)
                .build();

    }

    @Override
    public List<QuotationResponse> getQuotationsByPurchase(Integer purchaseId) {
        purchaseDAO.findById(purchaseId)
                .orElseThrow(() -> new InvalidDataException("Purchase request không tồn tại"));

        Map<Integer, String> supplierMap = supplierService.getSupplierIdToNameMap();

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
        List<QuotationDetail> details = quotationDetailDAO.findByQuotationId(quotationId);

        // map sang response cho template
        List<QuotationDetailResponse> quotationDetailResponses = details.stream()
                .map(quotationDetailMapper::toQuotationDetailResponse).toList();

        // lấy tên supplier từ helper
        Map<Integer, String> supplierMap = supplierService.getSupplierIdToNameMap();
        String supplierName = supplierMap.getOrDefault(q.getSupplierId(), "Supplier ko tồn tại");

        // Sử dụng OrderCalculationUtil.calculateBreakdown() để tính toán thay vì viết
        // logic tính toán trực tiếp
        BigDecimal[] breakdown = orderCalculationUtil.calculateBreakdown(details);
        BigDecimal subtotal = breakdown[0];
        BigDecimal totalDiscount = breakdown[1];
        BigDecimal totalTax = breakdown[2];
        BigDecimal grandTotal = breakdown[3];

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

    // search và filter cho màn quotation-list
    @Override
    public List<QuotationForPurchaseResponse> searchAndFilterForQuotation(SearchForQuotation s) {

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
        // báo giá
        Map<Integer, Object[]> summaryMap = purchaseDAO.findQuotationSummaryByFilter(s);

        List<QuotationForPurchaseResponse> out = new ArrayList<>();
        for (Map.Entry<Integer, Object[]> entry : summaryMap.entrySet()) {
            Object[] data = entry.getValue();

            // trả về list QuotationForPurchase
            out.add(QuotationForPurchaseResponse.builder()
                    .purchaseId(entry.getKey())
                    .needByDate((LocalDate) data[0])
                    .priority((String) data[1])
                    .numberOfQuotation((Integer) data[2])
                    .estPrice((BigDecimal) data[3])
                    .build());
        }
        return out;
    }

    //
    @Override
    public List<QuotationForPurchaseResponse> getQuotationAndPurchase() {
        return searchAndFilterForQuotation(new SearchForQuotation());
    }

    // method search cho màn qop
    @Override
    public List<QuotationResponse> quotationCriteriaForPurchase(QuotationSearchCriteria criteria) {

        // set mặc định trước
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

        // lấy ra listh quotation sau khí search
        List<Quotation> quotations = quotationDAO.searchAndFilterQuotationOfPurchase(criteria);

        // lấy toàn bọ supplier từ helper
        Map<Integer, String> supplierMap = supplierService.getSupplierIdToNameMap();

        // map về quotation response dùng helper
        return quotations.stream()
                .map(q -> toQuotationResponse(q, supplierMap))
                .toList();
    }

    // Chuyển đổi từ chi tiết yêu cầu mua sắm sang chi tiết báo giá - dùng trực tiếp
    // purchaseDetailDAO (bỏ PurchaseService)
    @Override
    public List<QuotationCreateDetailRequest> mapPurchaseToQuotation(Integer purchaseId) {

        // lấy trực tiếp từ DAO thay vì vòng vèo qua PurchaseService
        List<PurchaseDetail> prDetails = purchaseDetailDAO.findByPurchaseRequestId(purchaseId);

        return prDetails.stream()
                .map(pd -> {
                    String assetTypeName = pd.getAssetTypeId() != null
                            ? assetTypeService.findNameById(pd.getAssetTypeId())
                            : "Không tồn tại loại tài sản";

                    return QuotationCreateDetailRequest.builder()
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
