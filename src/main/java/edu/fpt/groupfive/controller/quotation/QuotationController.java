package edu.fpt.groupfive.controller.quotation;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.common.QuotationStatus;
import edu.fpt.groupfive.dto.request.*;
import edu.fpt.groupfive.dto.response.QuotationResponse;
import edu.fpt.groupfive.service.QuotationService;
import edu.fpt.groupfive.service.SupplierService;
import edu.fpt.groupfive.util.annotation.IsPurchaseStaff;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import edu.fpt.groupfive.dto.response.PurchaseDetailResponse;
import edu.fpt.groupfive.service.PurchaseService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService quotationService;
    private final SupplierService supplierService;

    // show form add
    @IsPurchaseStaff
    @GetMapping("/create/purchase-{id}/quotation")
    public String showQuotationForm(@PathVariable("id") Integer purchaseId, Model model) {
        QuotationCreateRequest quotationCreateRequest = new QuotationCreateRequest();
        quotationCreateRequest.setPurchaseRequestId(purchaseId);

        List<QuotationCreateDetailRequest> quoDetails = quotationService.mapPurchaseToQuotation(purchaseId);

        quotationCreateRequest.setQuotationCreateDetailRequestList(quoDetails);

        model.addAttribute("quotationCreateRequest", quotationCreateRequest);
        model.addAttribute("purchaseId", purchaseId);
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("activeMenu", "purchase");
        return "quotation/quotation-form";
    }

    // edit quotation
    @GetMapping("/quotations/{id}/edit")
    public String showEditQuotationForm(@PathVariable("id") Integer id, Model model) {
        QuotationCreateRequest quotationCreateRequest = quotationService.getQuotationRequestById(id);

        model.addAttribute("quotationCreateRequest", quotationCreateRequest);
        model.addAttribute("purchaseId", quotationCreateRequest.getPurchaseRequestId());
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("activeMenu", "quotation");
        return "quotation/quotation-form";
    }

    // xử lí form
    @PostMapping("/create/purchase-{id}/quotation")
    public String createQuotation(@PathVariable("id") Integer purchaseId,
            @Valid @ModelAttribute("quotationCreateRequest") QuotationCreateRequest quotationCreateRequest,
            BindingResult bindingResult,
            @RequestParam(value = "actions", required = false) String action,
            @RequestParam(value = "addDetail", required = false) Integer addIndex,
            @RequestParam(value = "removeDetail", required = false) Integer removeIndex,
            Model model) {

        // thêm 1 dòng detail mới
        if (addIndex != null) {

            // lấy ra dòng quotation detail gốc cần thêm mới
            QuotationCreateDetailRequest original = quotationCreateRequest.getQuotationCreateDetailRequestList()
                    .get(addIndex);

            // tạo 1 dòng quotation detail tương ứng
            QuotationCreateDetailRequest duplicate = QuotationCreateDetailRequest.builder()
                    .purchaseRequestDetailId(original.getPurchaseRequestDetailId())
                    .assetTypeName(original.getAssetTypeName())
                    .specificationRequirement(original.getSpecificationRequirement())
                    .quantity(original.getQuantity())
                    .warrantyMonths(original.getWarrantyMonths())
                    .price(original.getPrice())
                    .taxRate(original.getTaxRate())
                    .discountRate(original.getDiscountRate())
                    .build();
            quotationCreateRequest.getQuotationCreateDetailRequestList().add(addIndex + 1, duplicate);

            model.addAttribute("purchaseId", purchaseId);
            model.addAttribute("suppliers", supplierService.getAllSupplier());
            model.addAttribute("activeMenu", "purchase");
            return "quotation/quotation-form";
        }

        // xoóa 1 dòng quotation detail
        if (removeIndex != null) {
            quotationCreateRequest.getQuotationCreateDetailRequestList().remove(removeIndex.intValue());
            model.addAttribute("purchaseId", purchaseId);
            model.addAttribute("suppliers", supplierService.getAllSupplier());
            model.addAttribute("activeMenu", "purchase");
            return "quotation/quotation-form";
        }

        // nếu có lỗi thì đẩy lại
        if (bindingResult.hasErrors()) {
            model.addAttribute("purchaseId", purchaseId);
            model.addAttribute("suppliers", supplierService.getAllSupplier());
            model.addAttribute("activeMenu", "purchase");
            return "quotation/quotation-form";
        }

        quotationService.createQuotation(quotationCreateRequest, purchaseId, action);
        return "redirect:/purchases/" + purchaseId + "/purchase-detail";
    }

    // reject quotation
    @PostMapping("/quotations/{id}/reject")
    public String rejectQuotation(@PathVariable("id") Integer id,
            @RequestParam(value = "reason", required = false) String reason) {
        quotationService.rejectQuotation(id, reason);
        return "redirect:/quotations/" + id;
    }

    // show list quotation của purhcase cụ thể
    @GetMapping("/view/view-quotation-of-purchase/{purchaseId}")
    public String viewQuotationList(@PathVariable("purchaseId") Integer purchaseId, Model model) {
        List<QuotationResponse> quotations = quotationService.getQuotationsByPurchase(purchaseId);

        model.addAttribute("statuses", QuotationStatus.values());
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("activeMenu", "quotation");
        model.addAttribute("activeSub", "qt");
        model.addAttribute("quotations", quotations);
        model.addAttribute("criteria", new QuotationSearchCriteria());

        return "quotation/quotation-of-purchase";
    }

    // show detail
    @GetMapping("view/quotations/{id}/quotation-detail")
    public String getQuotation(@PathVariable("id") Integer id, Model model,
            jakarta.servlet.http.HttpServletRequest request) {
        QuotationResponse quotation = quotationService.getQuotationById(id);
        model.addAttribute("quotation", quotation);
        model.addAttribute("activeMenu", "quotation");
        model.addAttribute("activeSub", "qt");
        return "quotation/quotation-detail";
    }

    // ssearch quotation cho màn quotation-of-purchase
    @GetMapping("/search/quotation-of-purchase/{purchaseId}")
    public String searchQuotationOfPurchase(@PathVariable("purchaseId") Integer purchaseId,
            @ModelAttribute("criteria") QuotationSearchCriteria criteria,
            Model model) {
        criteria.setPurchaseId(purchaseId);

        model.addAttribute("purchaseId", purchaseId);
        model.addAttribute("statuses", QuotationStatus.values());
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("activeMenu", "quotation");
        model.addAttribute("activeSub", "qt");
        model.addAttribute("quotations", quotationService.quotationCriteriaForPurchase(criteria));

        return "quotation/quotation-of-purchase";
    }

    // search and filter cho header quotaion
    @GetMapping("/search/quotation-for-purchase")
    public String searchQuotation(@ModelAttribute("searchForQuotation") SearchForQuotation searchForQuotation,
            Model model) {
        model.addAttribute("activeMenu", "quotation");
        model.addAttribute("activeSub", "qt");
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("quotations", quotationService.searchAndFilterForQuotation(searchForQuotation));
        return "quotation/quotation-list";
    }

    // show list các quotation theo purchase
    @GetMapping("show/quotations-from-purchase")
    public String showQuotations(Model model, jakarta.servlet.http.HttpServletRequest request) {
        model.addAttribute("activeMenu", "quotation");
        model.addAttribute("activeSub", "qt");
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("quotations", quotationService.getQuotationAndPurchase());
        return "quotation/quotation-list";
    }

    // khởi tạo bind objetc
    @ModelAttribute("searchForQuotation")
    public SearchForQuotation initSearchForQuotation() {
        return new SearchForQuotation();
    }
}
