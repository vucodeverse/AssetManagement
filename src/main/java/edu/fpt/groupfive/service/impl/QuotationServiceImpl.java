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
import edu.fpt.groupfive.util.RangeAmount;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@Slf4j(topic = "QUOTATION-SERVICE")
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

    @Override
    public void createQuotation(QuotationCreateRequest quotationCreateRequest, Integer purchaseId, String action) {

        // ktra quaotion có tồn tại và dc approve chưa
        purchaseDAO.findByIdAndApproved(purchaseId, "APPROVED")
                .orElseThrow(() -> new InvalidDataException("Purchase request này không tồn tại hoặc chưa được chấp " +
                        "nhận"));

        // lấy ra list purchase detail của purchase request nhận vào
        List<PurchaseDetail> details = purchaseDetailDAO.findByPurchaseRequestId(purchaseId);

        // dùng map để check qd có tồn tại hay ko
        Map<Integer, PurchaseDetail> purchaseDetailMap = new HashMap<>();

        // value id - key detail
        for (PurchaseDetail d : details) {
            purchaseDetailMap.put(d.getId(), d);
        }

        // map quotation create về quotaiton
        Quotation q = quotationMapper.toQuotation(quotationCreateRequest);
        q.setPurchaseId(purchaseId);

        // kiểm tra xem save hay draft
        QuotationStatus quotationStatus = "draft".equalsIgnoreCase(action) ? QuotationStatus.DRAFT
                : QuotationStatus.PENDING;

        // set các giá trị
        q.setTotalAmount(calculateTotal(quotationCreateRequest));
        q.setUpdatedAt(LocalDate.now());

        Integer quotationId;

        // chekc xem quotaiton này đã được tạo hay chưa
        // nếu r lấy luon id đó để update
        // ko thì tọa mưới
        if (quotationCreateRequest.getQuotationId() != null) {
            quotationId = quotationCreateRequest.getQuotationId();
            q.setId(quotationId);
            q.setQuotationStatus(quotationStatus);
            quotationDAO.update(q);
            quotationDetailDAO.deleteByQuotationId(quotationId);
        } else {
            q.setQuotationStatus(quotationStatus);
            q.setCreatedAt(LocalDate.now());
            quotationId = quotationDAO.insert(q);
        }

        // check xem id có hợp lệ hay ko
        if (quotationId != 0) {

            // duyệt qua từng quotation detail
            for (QuotationCreateDetailRequest qd : quotationCreateRequest.getQuotationCreateDetailRequestList()) {

                // ktra purchae detail có tồn tại hay ko
                PurchaseDetail pd = purchaseDetailMap.get(qd.getPurchaseRequestDetailId());
                if (pd == null)
                    throw new InvalidDataException("Invalid purchaseDetailId");

                // map sang quotaiton detial
                QuotationDetail quotationDetail = quotationDetailMapper.toQuotationDetail(qd);
                quotationDetail.setQuotationId(quotationId);
                quotationDetail.setPurchaseDetailId(pd.getId());
                if (pd.getAssetTypeId() != null) {
                    quotationDetail.setAssetTypeId(pd.getAssetTypeId());
                }
                quotationDetailDAO.insert(quotationDetail);
            }
        }
    }


    // tính total
    private BigDecimal calculateTotal(QuotationCreateRequest request) {
        BigDecimal total = BigDecimal.ZERO;

        // duyệt từng detail 1
        for (QuotationCreateDetailRequest quotationCreateDetailRequest : request.getQuotationCreateDetailRequestList()) {
            if (quotationCreateDetailRequest.getPrice() != null && quotationCreateDetailRequest.getQuantity() != null) {

                // parse
                BigDecimal qty = BigDecimal.valueOf(quotationCreateDetailRequest.getQuantity());
                BigDecimal price = quotationCreateDetailRequest.getPrice();

                // tính total của từng line
                BigDecimal lineSubtotal = qty.multiply(price);

                BigDecimal discountRate = quotationCreateDetailRequest.getDiscountRate() != null ? quotationCreateDetailRequest.getDiscountRate() : BigDecimal.ZERO;

                // tính discount cho từng line
                BigDecimal discount = lineSubtotal.multiply(discountRate).divide(BigDecimal.valueOf(100), 2,
                        RoundingMode.HALF_UP);

                // tính ra số tiền phải chịu thuế
                BigDecimal taxableAmount = lineSubtotal.subtract(discount);

                //lấy ra thuế
                BigDecimal taxRate = quotationCreateDetailRequest.getTaxRate() != null ? quotationCreateDetailRequest.getTaxRate() : BigDecimal.ZERO;

                // tnh thuế
                BigDecimal tax = taxableAmount.multiply(taxRate).divide(BigDecimal.valueOf(100), 2,
                        java.math.RoundingMode.HALF_UP);

                total = total.add(taxableAmount.add(tax));
            }
        }
        return total;
    }

    // lấy ra quotation đã save draft để sửa
    @Override
    public QuotationCreateRequest getQuotationRequestById(Integer id) {
        Quotation q = quotationDAO.findById(id)
                .orElseThrow(() -> new InvalidDataException("Quotation not found"));

        // map quotation detail sang response
        List<QuotationDetailResponse> details =
                quotationDetailDAO.findByQuotationId(q.getId()).stream().map(quotationDetailMapper::toQuotationDetailResponse).toList();

        List<QuotationCreateDetailRequest> quotationCreateDetailRequestList = new ArrayList<>();

        // duyệt từng detailxá
        for (QuotationDetailResponse d : details) {

            //add quotaiton detail đã dc tạo vào quotaiton create list
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
        quotationDAO.updateStatus(id, QuotationStatus.REJECTED, reason);
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

        // chuyen tu purchase detail vào quotation create
        for (PurchaseDetail purchaseDetail : purchaseDetailList) {

            // set san id va quantity
            QuotationCreateDetailRequest item = QuotationCreateDetailRequest.builder()
                    .purchaseRequestDetailId(purchaseDetail.getId())
                    .quantity(purchaseDetail.getQuantity())
                    .assetTypeName(purchaseDetail.getAssetTypeName())
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

        // lấy ra list detail response
        List<QuotationDetailResponse> quotationDetailResponses = quotationDetailDAO
                .findByQuotationId(quotationId).stream().map(quotationDetailMapper::toQuotationDetailResponse).toList();

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

        // duyệt từng quotation detail response
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

            // tách  min và max amount
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

        List<QuotationCreateDetailRequest> quoDetails = prDetails.stream()
                .map(pd -> QuotationCreateDetailRequest.builder()
                        .purchaseRequestDetailId(pd.getId())
                        .assetTypeName(pd.getAssetTypeName())
                        .specificationRequirement(pd.getSpecification())
                        .quantity(pd.getQuantity())
                        .price(BigDecimal.ZERO)
                        .taxRate(BigDecimal.TEN)
                        .discountRate(BigDecimal.ZERO)
                        .quotationDetailNote(pd.getSpecification())
                        .build())
                .collect(Collectors.<QuotationCreateDetailRequest>toList());
        return quoDetails;
    }

}
