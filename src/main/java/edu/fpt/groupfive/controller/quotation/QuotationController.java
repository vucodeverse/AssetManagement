package edu.fpt.groupfive.controller.quotation;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.common.QuotationStatus;
import edu.fpt.groupfive.dto.request.*;
import edu.fpt.groupfive.dto.response.QuotationResponse;
import edu.fpt.groupfive.service.QuotationService;
import edu.fpt.groupfive.service.SupplierService;
import edu.fpt.groupfive.util.annotation.IsPurchaseStaff;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/purchase-staff/quotations")
public class QuotationController {

    private final QuotationService quotationService;
    private final SupplierService supplierService;

    // Hiển thị form tạo
    @IsPurchaseStaff
    @GetMapping("/create/{purchaseId}")
    public String showQuotationForm(@PathVariable("purchaseId") Integer purchaseId, Model model) {
        QuotationCreateRequest quotationCreateRequest = new QuotationCreateRequest();
        quotationCreateRequest.setPurchaseId(purchaseId);

        List<QuotationDetailCreateRequest> quoDetails = quotationService.mapPurchaseToQuotation(purchaseId);

        quotationCreateRequest.setQuotationDetailCreateRequests(quoDetails);

        model.addAttribute("quotationCreateRequest", quotationCreateRequest);
        model.addAttribute("purchaseId", purchaseId);
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("activeMenu", "purchase");
        return "quotation/quotation-form";
    }

    // Chỉnh sửa báo giá khi draft
    @GetMapping("/{quotationId}/edit")
    public String showEditQuotationForm(@PathVariable("quotationId") Integer id, Model model) {
        QuotationCreateRequest quotationCreateRequest = quotationService.getQuotationRequestById(id);

        model.addAttribute("quotationCreateRequest", quotationCreateRequest);
        model.addAttribute("purchaseId", quotationCreateRequest.getPurchaseId());
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("activeMenu", "quotation");
        return "quotation/quotation-form";
    }

    // xử lí form
    @PostMapping("/create/{id}")
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
            QuotationDetailCreateRequest original = quotationCreateRequest.getQuotationDetailCreateRequests()
                    .get(addIndex);

            // tạo 1 dòng quotation detail tương ứng
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
            quotationCreateRequest.getQuotationDetailCreateRequests().add(addIndex + 1, duplicate);

            model.addAttribute("purchaseId", purchaseId);
            model.addAttribute("suppliers", supplierService.getAllSupplier());
            model.addAttribute("activeMenu", "purchase");
            return "quotation/quotation-form";
        }

        // xoóa 1 dòng quotation detail
        if (removeIndex != null) {
            quotationCreateRequest.getQuotationDetailCreateRequests().remove(removeIndex.intValue());
            model.addAttribute("purchaseId", purchaseId);
            model.addAttribute("suppliers", supplierService.getAllSupplier());
            model.addAttribute("activeMenu", "purchase");
            return "quotation/quotation-form";
        }

        // nếu có lỗi thì đẩy lại
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> {
                System.out.println("Validation error: " + error.getDefaultMessage());
            });
            model.addAttribute("purchaseId", purchaseId);
            model.addAttribute("suppliers", supplierService.getAllSupplier());
            model.addAttribute("activeMenu", "purchase");
            return "quotation/quotation-form";
        }

        quotationService.createQuotation(quotationCreateRequest, purchaseId, action);
        return "redirect:/purchase-staff/purchases/" + purchaseId + "/purchase-detail";
    }

    // Từ chối báo giá
    @PostMapping("/{id}/reject")
    public String rejectQuotation(@PathVariable("id") Integer id,
            @RequestParam(value = "reason", required = false) String reason) {
        quotationService.rejectQuotation(id, reason);
        return "redirect:/purchase-staff/quotations/" + id;
    }

    // show list quotation của purhcase cụ thể
    @GetMapping("/of-purchase/{purchaseId}")
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

    // Hiển thị chi tiết báo giá
    @GetMapping("/{id}")
    public String getQuotation(@PathVariable("id") Integer id, Model model) {
        QuotationResponse quotation = quotationService.getQuotationById(id);
        model.addAttribute("quotation", quotation);
        model.addAttribute("activeMenu", "quotation");
        model.addAttribute("activeSub", "qt");
        return "quotation/quotation-detail";
    }

    // Tìm kiếm báo giá cho màn hình danh quotation of purcahse
    @GetMapping("/of-purchase/{purchaseId}/search")
    public String searchQuotationOfPurchase(@PathVariable("purchaseId") Integer purchaseId,
            @Valid @ModelAttribute("criteria") QuotationSearchCriteria criteria,
            BindingResult result,
            Model model) {

        if(result.hasErrors()) {
            return "quotation/quotation-of-purchase";
        }
        criteria.setPurchaseId(purchaseId);

        model.addAttribute("purchaseId", purchaseId);
        model.addAttribute("statuses", QuotationStatus.values());
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("activeMenu", "quotation");
        model.addAttribute("activeSub", "qt");
        model.addAttribute("quotations", quotationService.quotationCriteriaForPurchase(criteria));

        return "quotation/quotation-of-purchase";
    }

    // Tìm kiếm và lọc cho danh sách báo giá chung
    @GetMapping("/search")
    public String searchQuotation(
            @Valid @ModelAttribute("searchForQuotation") QuotationSearchCriteria quotationSearchCriteria,
            BindingResult bindingResult,
            Model model) {
        model.addAttribute("activeMenu", "quotation");
        model.addAttribute("activeSub", "qt");
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("status", QuotationStatus.values());

        if (bindingResult.hasErrors()) {
            model.addAttribute("quotations", quotationService.getQuotationAndPurchase());
            return "quotation/quotation-list";
        }

        model.addAttribute("quotations", quotationService.searchAndFilterForQuotation(quotationSearchCriteria));
        return "quotation/quotation-list";
    }

    // show list các quotation theo purchase
    @GetMapping("")
    public String showQuotations(Model model) {
        model.addAttribute("activeMenu", "quotation");
        model.addAttribute("activeSub", "qt");
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("status", QuotationStatus.values());
        model.addAttribute("quotations", quotationService.getQuotationAndPurchase());
        return "quotation/quotation-list";
    }

    // Khởi tạo đối tượng tìm kiếm khi bắt đầu bind dữ liệu
    @ModelAttribute("searchForQuotation")
    public QuotationSearchCriteria initSearchForQuotation() {
        return new QuotationSearchCriteria();
    }
}
