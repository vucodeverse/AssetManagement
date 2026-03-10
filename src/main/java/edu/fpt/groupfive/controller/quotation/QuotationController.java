package edu.fpt.groupfive.controller.quotation;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.common.QuotationStatus;
import edu.fpt.groupfive.dto.request.*;
import edu.fpt.groupfive.dto.response.QuotationResponse;
import edu.fpt.groupfive.dto.response.QuotationSummaryResponse;
import edu.fpt.groupfive.service.QuotationService;
import edu.fpt.groupfive.service.SupplierService;
import edu.fpt.groupfive.util.annotation.IsDirector;
import edu.fpt.groupfive.util.annotation.IsPurchaseStaff;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/quotations")
public class QuotationController {

    private static final String URL_PURCHASE_OF_DETAIL = "quotation/quotation-of-purchase";
    private static final String URL_QUOTATION_LIST = "quotation/quotation-list";
    private static final String URL_QUOTATION_FORM = "quotation/quotation-form";

    private final QuotationService quotationService;
    private final SupplierService supplierService;

    // show form tạo quotation
    @IsPurchaseStaff
    @GetMapping("/create/{purchaseId}")
    public String showQuotationForm(@PathVariable("purchaseId") Integer purchaseId, Model model) {

        QuotationCreateRequest quotationCreateRequest = new QuotationCreateRequest();
        quotationCreateRequest.setPurchaseId(purchaseId);

        // map purchase detail sang quotation detail
        List<QuotationDetailCreateRequest> details = quotationService.mapPurchaseToQuotation(purchaseId);

        quotationCreateRequest.setQuotationDetailCreateRequests(details);

        model.addAttribute("quotationCreateRequest", quotationCreateRequest);
        prepareQuotationFormModel(model, purchaseId);

        return URL_QUOTATION_FORM;
    }

    // form update quotation
    @IsPurchaseStaff
    @GetMapping("/{quotationId}/edit")
    public String showEditQuotationForm(@PathVariable("quotationId") Integer quotationId, Model model) {

        QuotationCreateRequest request = quotationService.getQuotationRequestById(quotationId);

        model.addAttribute("quotationCreateRequest", request);
        prepareQuotationFormModel(model, request.getPurchaseId());

        return URL_QUOTATION_FORM;
    }

    // xử lí form tạo quotaiton
    @IsPurchaseStaff
    @PostMapping("/create/{purchaseId}")
    public String createQuotation(
            @PathVariable("purchaseId") Integer purchaseId,
            @Valid @ModelAttribute("quotationCreateRequest") QuotationCreateRequest request,
            BindingResult result,
            @RequestParam(value = "actions", required = false) String action,
            @RequestParam(value = "addDetail", required = false) Integer addIndex,
            @RequestParam(value = "removeDetail", required = false) Integer removeIndex,
            Model model,
            RedirectAttributes redirectAttributes) {

        // xử lí việc thêm 1 dùng detail mới
        if (addIndex != null) {
            addQuotationDetailRow(request, addIndex);
            prepareQuotationFormModel(model, purchaseId);
            return URL_QUOTATION_FORM;
        }

        // xóa 1 dùng detail
        if (removeIndex != null) {
            request.getQuotationDetailCreateRequests().remove(removeIndex.intValue());
            prepareQuotationFormModel(model, purchaseId);
            return URL_QUOTATION_FORM;
        }

        // trả về form nếu có lỗi
        if (result.hasErrors()) {
            prepareQuotationFormModel(model, purchaseId);
            return URL_QUOTATION_FORM;
        }

        // tạo quotation
        Integer quotationId = quotationService.createQuotation(request, purchaseId, action);

        if (quotationId != null) {
            redirectAttributes.addFlashAttribute("message", "Thêm báo giá thành công");
        }

        return "redirect:/quotations/" + quotationId;
    }

    // xử lí các actions
    @PostMapping("/{id}/actions")
    public String formActions(@PathVariable("id") Integer id,
            @RequestParam("action") String action,
            @RequestParam(value = "reason", required = false) String reason,
            RedirectAttributes redirectAttributes) {

        int purchaseId = quotationService.getQuotationById(id).getPurchaseId();
        quotationService.actionWithQuota(id, action, reason);

        redirectAttributes.addFlashAttribute("message", "Thay đổi báo giá thành công");
        return "redirect:/quotations/of-purchase/" + purchaseId;
    }

    // hiển thị ra list quotation của từng purchase reuqest
    @GetMapping("/of-purchase/{purchaseId}")
    public String viewQuotationList(
            @PathVariable("purchaseId") Integer purchaseId,
            Model model) {

        // lấy ra list quotation
        List<QuotationResponse> quotations = quotationService.getQuotationsByPurchase(purchaseId);

        prepareQuotationFilter(model);

        model.addAttribute("quotations", quotations);
        model.addAttribute("criteria", new QuotationSearchCriteria());

        return URL_PURCHASE_OF_DETAIL;
    }

    // hiển thị quotation chi tiết
    @GetMapping("/{id}")
    public String getQuotation(@PathVariable("id") Integer id, Model model) {

        QuotationResponse quotation = quotationService.getQuotationById(id);

        model.addAttribute("quotation", quotation);
        prepareQuotationMenu(model);

        return "quotation/quotation-detail";
    }

    // tìm kiếm tại màn quotation-of-purchase
    @GetMapping("/of-purchase/{purchaseId}/search")
    public String searchQuotationOfPurchase(
            @PathVariable("purchaseId") Integer purchaseId,
            @Valid @ModelAttribute("criteria") QuotationSearchCriteria criteria,
            BindingResult result,
            Model model) {

        prepareQuotationFilter(model);

        if (result.hasErrors()) {
            return URL_PURCHASE_OF_DETAIL;
        }

        criteria.setPurchaseId(purchaseId);

        model.addAttribute("quotations",
                quotationService.quotationCriteriaForPurchase(criteria));

        return URL_PURCHASE_OF_DETAIL;
    }

    // search cho màn quotaiton-list
    @GetMapping("/search")
    public String searchQuotation(
            @Valid @ModelAttribute("searchForQuotation") QuotationSearchCriteria criteria,
            BindingResult result,
            Model model) {

        prepareQuotationSearchModel(model);
        List<QuotationSummaryResponse> quotationResponses;
        if (result.hasErrors()) {
            quotationResponses = quotationService.getQuotationAndPurchase();
        } else {
            quotationResponses = quotationService.searchAndFilterForQuotation(criteria);
        }

        model.addAttribute("quotations", quotationResponses);

        return URL_QUOTATION_LIST;
    }

    // hiển thị quotation summary
    @GetMapping("")
    public String showQuotations(Model model) {

        prepareQuotationSearchModel(model);

        model.addAttribute("quotations",
                quotationService.getQuotationAndPurchase());

        return URL_QUOTATION_LIST;
    }

    // khởi tọa bind object
    @ModelAttribute("searchForQuotation")
    public QuotationSearchCriteria initSearchForQuotation() {
        return new QuotationSearchCriteria();
    }

    private void prepareQuotationFormModel(Model model, Integer purchaseId) {
        model.addAttribute("purchaseId", purchaseId);
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("activeMenu", "approval");
    }

    // set navbar
    private void prepareQuotationMenu(Model model) {
        model.addAttribute("activeMenu", "approval");
        model.addAttribute("activeSub", "qt");
    }

    // filter
    private void prepareQuotationFilter(Model model) {
        prepareQuotationMenu(model);
        model.addAttribute("statuses", QuotationStatus.values());
        model.addAttribute("suppliers", supplierService.getAllSupplier());
    }

    // filter
    private void prepareQuotationSearchModel(Model model) {
        prepareQuotationMenu(model);
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("status", QuotationStatus.values());
    }

    // thêm mới 1 dòng quotaton detail
    private void addQuotationDetailRow(
            QuotationCreateRequest request,
            Integer index) {

        QuotationDetailCreateRequest original = request.getQuotationDetailCreateRequests().get(index);

        QuotationDetailCreateRequest duplicate = QuotationDetailCreateRequest.builder()
                .purchaseRequestDetailId(original.getPurchaseRequestDetailId())
                .assetTypeName(original.getAssetTypeName())
                .specificationRequirement(original.getSpecificationRequirement())
                .quantity(original.getQuantity())
                .warrantyMonths(original.getWarrantyMonths())
                .price(original.getPrice())
                .taxRate(original.getTaxRate())
                .discountRate(original.getDiscountRate())
                .build();

        request.getQuotationDetailCreateRequests().add(index + 1, duplicate);
    }
}