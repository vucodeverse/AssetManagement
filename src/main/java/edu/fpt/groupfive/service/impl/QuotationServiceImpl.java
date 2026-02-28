package edu.fpt.groupfive.service.impl;

import edu.fpt.groupfive.common.QuotationStatus;
import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dao.*;
import edu.fpt.groupfive.dto.request.QuotationCreateDetailRequest;
import edu.fpt.groupfive.dto.request.QuotationCreateRequest;
import edu.fpt.groupfive.dto.request.QuotationSearchCriteria;
import edu.fpt.groupfive.dto.request.SearchForQuotation;
import edu.fpt.groupfive.dto.response.PurchaseDetailResponse;
import edu.fpt.groupfive.dto.response.QuotationDetailResponse;
import edu.fpt.groupfive.dto.response.QuotationForPurchaseResponse;
import edu.fpt.groupfive.dto.response.QuotationResponse;
import edu.fpt.groupfive.mapper.QuotationDetailMapper;
import edu.fpt.groupfive.mapper.QuotationMapper;
import edu.fpt.groupfive.model.*;

import edu.fpt.groupfive.service.PurchaseService;
import edu.fpt.groupfive.service.QuotationService;
import edu.fpt.groupfive.util.OrderCalculationUtil;
import edu.fpt.groupfive.util.RangeAmount;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
    private final SupplierDAO supplierDAO;
    private final RangeAmount rangeAmount;
    private final PurchaseService purchaseService;
    private final OrderCalculationUtil orderCalculationUtil;

    @Override
    public void createQuotation(QuotationCreateRequest quotationCreateRequest, Integer purchaseId, String action) {

        // ktra purchase có tồn tại và dc approve chưa
        purchaseDAO.findByIdAndApproved(purchaseId, "APPROVED")
                .orElseThrow(() -> new InvalidDataException("Purchase request này không tồn tại hoặc chưa được chấp " +
                        "nhận"));

        // lấy ra list purchase detail của purchase request nhận vào
        List<PurchaseDetail> details = purchaseDetailDAO.findByPurchaseRequestId(purchaseId);

        // dùng map để check pd có tồn tại hay ko
        Map<Integer, PurchaseDetail> purchaseDetailMap = new HashMap<>();

        // value id - key detail
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

        // build list quotation detail trước
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

        // gắn detail list vào quotation → DAO sẽ insert/update tất cả trong 1
        // transaction
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

        // duyệt từng detailxá
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

        Map<Integer, String> map = new HashMap<>();
        for (Supplier s : supplierDAO.getAllSupplier()) {
            map.put(s.getId(), s.getSupplierName());
        }

        // list detail quotation
        List<QuotationCreateDetailRequest> quotationCreateDetailRequestList = new ArrayList<>();

        // chuyen tu purchase detail vào quotation create
        for (PurchaseDetail purchaseDetail : purchaseDetailList) {

            // set san id va quantity
            QuotationCreateDetailRequest item = QuotationCreateDetailRequest.builder()
                    .purchaseRequestDetailId(purchaseDetail.getId())
                    .quantity(purchaseDetail.getQuantity())
                    .assetTypeName(map.getOrDefault(purchaseDetail.getAssetTypeId(), "Không tồn tại nhà cung cấp"))
                    .specificationRequirement(purchaseDetail.getSpecificationRequirement())
                    .build();

            quotationCreateDetailRequestList.add(item);
        }

        // trả bè quotation request
        return QuotationCreateRequest.builder()
                .purchaseRequestId(purchaseId)
                .quotationCreateDetailRequestList(quotationCreateDetailRequestList)
                .build();

    }

    @Override
    public List<QuotationResponse> getQuotationsByPurchase(Integer purchaseId) {
        purchaseDAO.findById(purchaseId)
                .orElseThrow(() -> new InvalidDataException("Purchase request không tồn tại"));

        List<Quotation> quotations = quotationDAO.findByPurchaseId(purchaseId);

        Map<Integer, String> supplierMap = new HashMap<>();
        for (Supplier s : supplierDAO.getAllSupplier()) {
            supplierMap.put(s.getId(), s.getSupplierName());
        }

        List<QuotationResponse> out = new ArrayList<>();
        for (Quotation q : quotations) {
            out.add(QuotationResponse.builder()
                    .quotationId(q.getId())
                    .purchaseId(q.getPurchaseId())
                    .quotationStatus(q.getQuotationStatus())
                    .totalAmount(q.getTotalAmount())
                    .createdAt(q.getCreatedAt())
                    .supplierName(supplierMap.getOrDefault(q.getSupplierId(), "Không tồn tại supplier"))
                    .rejectedReason(q.getRejectedReason())
                    .build());
        }
        return out;
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

        // lấy ra tên supplier name lọc theo id có trong quotation
        String supplierName = supplierDAO.getAllSupplier().stream()
                .filter(s -> s.getId().equals(q.getSupplierId()))
                .map(Supplier::getSupplierName)
                .findFirst()
                .orElse("Supplier ko tồn tại");

        // khởi tạo giá trị
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        // dùng details đã lấy ở trên (không dùng q.getQuotationDetails() vì nó null)
        for (QuotationDetail qd : details) {
            BigDecimal discountRate = qd.getDiscountRate() != null ? qd.getDiscountRate() : BigDecimal.ZERO;
            BigDecimal taxRate = qd.getTaxRate() != null ? qd.getTaxRate() : BigDecimal.ZERO;
            BigDecimal price = qd.getPrice();

            BigDecimal qty = BigDecimal.valueOf(qd.getQuantity());

            // tính total cho từng line
            BigDecimal lineTotal = qty.multiply(price);

            // tính discount cho từng line
            BigDecimal lineDiscount = lineTotal.multiply(discountRate).divide(BigDecimal.valueOf(100), 2,
                    RoundingMode.HALF_UP);

            // tính số tiền thuế
            BigDecimal taxableAmout = lineTotal.subtract(lineDiscount);

            // tính thuế
            BigDecimal tax = taxableAmout.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            subtotal = subtotal.add(lineTotal);
            totalDiscount = totalDiscount.add(lineDiscount);
            totalTax = totalTax.add(tax);
        }

        BigDecimal grandTotal = subtotal.subtract(totalDiscount).add(totalTax);

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

        // key: purchase id - value: object
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

        // lấy toàn bọ supplier lên trước
        Map<Integer, String> supplierMap = new HashMap<>();
        for (Supplier s : supplierDAO.getAllSupplier()) {
            supplierMap.put(s.getId(), s.getSupplierName());
        }

        // map về qutoation response.
        List<QuotationResponse> out = new ArrayList<>();
        for (Quotation q : quotations) {
            out.add(QuotationResponse.builder()
                    .quotationId(q.getId())
                    .purchaseId(q.getPurchaseId())
                    .quotationStatus(q.getQuotationStatus())
                    .totalAmount(q.getTotalAmount())
                    .createdAt(q.getCreatedAt())
                    .supplierName(supplierMap.getOrDefault(q.getSupplierId(), "Không tồn tại supplier "))
                    .rejectedReason(q.getRejectedReason())
                    .build());
        }
        return out;
    }

    // map từ purchase detau=il sang quotation detail
    @Override
    public List<QuotationCreateDetailRequest> mapPurchaseToQuotation(Integer purchaseId) {

        // lấy ra danh sách các pr detail response
        List<PurchaseDetailResponse> prDetails = purchaseService.findById(purchaseId).getPurchaseDetails();

        // map sang quoation detai

        return prDetails.stream()
                .map(pd -> QuotationCreateDetailRequest.builder()
                        .purchaseRequestDetailId(pd.getId())
                        .assetTypeName(pd.getAssetTypeName())
                        .specificationRequirement(pd.getSpecificationRequirement())
                        .quantity(pd.getQuantity())
                        .price(BigDecimal.ZERO)
                        .taxRate(BigDecimal.TEN)
                        .discountRate(BigDecimal.ZERO)
                        .quotationDetailNote(pd.getPurchaseDetailNote())
                        .build())
                .collect(Collectors.<QuotationCreateDetailRequest>toList());
    }

}
