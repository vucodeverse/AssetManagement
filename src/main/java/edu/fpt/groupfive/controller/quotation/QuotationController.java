package edu.fpt.groupfive.controller.quotation;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.common.PurchaseProcessStatus;
import edu.fpt.groupfive.dto.request.*;
import edu.fpt.groupfive.dto.response.PurchaseRequestResponse;
import edu.fpt.groupfive.dto.response.QuotationResponse;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.ISupplierService;
import edu.fpt.groupfive.service.PurchaseService;
import edu.fpt.groupfive.service.QuotationService;
import edu.fpt.groupfive.util.annotation.IsPurchaseStaff;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/quotations")
public class QuotationController {

    private static final String URL_QUO_OF_PURCHASE = "quotation/quotation-of-purchase";
    private static final String URL_QUOTATION_FORM = "quotation/quotation-form";

    private final QuotationService quotationService;
    private final ISupplierService supplierService;
    private final AssetTypeService assetTypeService;
    private final PurchaseService purchaseService;

    @Value("${quotation.success.detail_action}")
    private String successDetailActionMsg;

    @Value("${quotation.success.create_flash}")
    private String successCreateMsg;

    @Value("${quotation.success.action_flash}")
    private String successActionMsg;

    // hiển thị trang compare quotation
    @PreAuthorize("hasAnyAuthority('DIRECTOR', 'PURCHASE_STAFF')")
    @GetMapping("/compare")
    public String showCompare(@RequestParam(value = "ids", required = false) String ids,
            @RequestParam(value = "purchaseId", required = false) Integer purchaseId,
            Model model) {
        PurchaseRequestResponse purchaseRequestResponse = purchaseService.getPurchaseRequestById(purchaseId);
        model.addAttribute("purchaseDetails", purchaseRequestResponse.getPurchaseDetails());
        model.addAttribute("purchaseId", purchaseId);

        if (ids == null || ids.isBlank()) {
            return "redirect:/quotations/of-purchase/" + purchaseId;
        }

        List<Integer> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        // chỉ lấy ra các quotation detial ko bị reject
        List<QuotationResponse> quotationResponses = idList.stream()
                .map(quotationService::getQuotationById)
                .map(qr -> {
                    qr.setQuotationDetails(
                            qr.getQuotationDetails().stream()
                                    .filter(qd -> qd.getStatus() != PurchaseProcessStatus.REJECTED)
                                    .toList());
                    return qr;
                })
                .toList();
        model.addAttribute("quotations", quotationResponses);
        model.addAttribute("purchaseModel", "PR-" + purchaseId);

        return "quotation/quotation-compare";

    }

    // duyt or reject quotation detail
    @PostMapping("/details/{id}/actions")
    public String processQuotationDetail(@PathVariable("id") Integer id, @RequestParam("actions") String actions,
            @RequestParam("qoId") Integer qoId,
            RedirectAttributes redirectAttributes, @RequestHeader(value = "Referer", required = false) String referer) {

        quotationService.processQuotationDetailAction(id, actions, qoId);
        redirectAttributes.addFlashAttribute("message", successDetailActionMsg);
        return (referer != null) ? "redirect:" + referer : "redirect:/quotations";

    }

    // show form tạo quotation
    @IsPurchaseStaff
    @GetMapping("/create/{purchaseId}")
    public String showQuotationForm(@PathVariable("purchaseId") Integer purchaseId, Model model,
            RedirectAttributes redirectAttributes) {

        QuotationCreateRequest quotationCreateRequest = new QuotationCreateRequest();
        quotationCreateRequest.setPurchaseId(purchaseId);
        try {

            // map purchase detail sang quotation detail
            List<QuotationDetailCreateRequest> details = quotationService.prepareQuotationForm(purchaseId);
            quotationCreateRequest.setQuotationDetailCreateRequests(details);
            model.addAttribute("quotationCreateRequest", quotationCreateRequest);
            prepareQuotationFormModel(model, purchaseId);
            return URL_QUOTATION_FORM;
        } catch (InvalidDataException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/quotations/of-purchase/" + purchaseId;
        }
    }

    // form update quotation
    @IsPurchaseStaff
    @GetMapping("/{quotationId}/edit")
    public String showEditQuotationForm(@PathVariable("quotationId") Integer quotationId, Model model) {

        QuotationCreateRequest request = quotationService.prepareQuotationUpdateForm(quotationId);

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

            if (removeIndex >= 0 && request.getQuotationDetailCreateRequests().size() > 1) {
                request.getQuotationDetailCreateRequests().remove(removeIndex.intValue());
            }
            prepareQuotationFormModel(model, purchaseId);
            return URL_QUOTATION_FORM;
        }

        // trả về form nếu có lỗi
        if (result.hasErrors()) {
            prepareQuotationFormModel(model, purchaseId);
            return URL_QUOTATION_FORM;
        }
        Integer quotationId = null;
        // tạo quotation
        try {
            quotationId = quotationService.createQuotation(request, purchaseId, action);
        } catch (InvalidDataException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/quotations/of-purchase/" + purchaseId;
        }
        if (quotationId != null) {
            redirectAttributes.addFlashAttribute("message", successCreateMsg);
        }

        return "redirect:/quotations/" + quotationId;
    }

    // xử lí các actions
    @PostMapping("/{id}/actions")
    public String processActions(@PathVariable("id") Integer id,
            @RequestParam("action") String action,
            RedirectAttributes redirectAttributes) {

        // lấy ra purchase id của quotation.
        int purchaseId = quotationService.getQuotationById(id).getPurchaseId();
        try {
            quotationService.processQuotationAction(id, action);
            redirectAttributes.addFlashAttribute("message", successActionMsg);
        } catch (InvalidDataException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/quotations/of-purchase/" + purchaseId;
    }

    // hiển thị ra list quotation của từng purchase reuqest
    @GetMapping("/of-purchase/{purchaseId}")
    public String viewQuotationOfPurchase(
            @PathVariable("purchaseId") Integer purchaseId,
            Model model) {

        // lấy ra list quotation
        List<QuotationResponse> quotations = quotationService.getQuotationsByPurchaseId(purchaseId);

        prepareQuotationFilter(model);

        model.addAttribute("purchaseId", purchaseId);
        model.addAttribute("purchaseStatus", purchaseService.getPurchaseRequestById(purchaseId).getStatus());
        model.addAttribute("quotations", quotations);
        model.addAttribute("criteria", new QuotationSearchCriteria());

        return URL_QUO_OF_PURCHASE;
    }

    // hiển thị quotation chi tiết
    @GetMapping("/{id}")
    public String getQuotationDetail(@PathVariable("id") Integer id, Model model) {

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
            return URL_QUO_OF_PURCHASE;
        }

        criteria.setPurchaseId(purchaseId);

        model.addAttribute("purchaseId", purchaseId);
        model.addAttribute("purchaseStatus", purchaseService.getPurchaseRequestById(purchaseId).getStatus());
        model.addAttribute("quotations",
                quotationService.searchQuotationsByPurchaseId(criteria));

        return URL_QUO_OF_PURCHASE;
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
        model.addAttribute("assetTypes", assetTypeService.getAllAssetType());
    }

    // set navbar
    private void prepareQuotationMenu(Model model) {
        model.addAttribute("activeMenu", "approval");
        model.addAttribute("activeSub", "qt");
    }

    // filter
    private void prepareQuotationFilter(Model model) {
        prepareQuotationMenu(model);
        model.addAttribute("statuses", PurchaseProcessStatus.values());
        model.addAttribute("suppliers", supplierService.getAllSupplier());
    }

    // thêm mới 1 dòng quotaton detail
    private void addQuotationDetailRow(
            QuotationCreateRequest request,
            Integer index) {

        // lấy chính dòng hiện tại cần thêm mới ra
        QuotationDetailCreateRequest original = request.getQuotationDetailCreateRequests().get(index);

        // tạo dòng mới bằng việc duplicate từ dòng cũ sang
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

        // thêm dòng mới vào
        request.getQuotationDetailCreateRequests().add(index + 1, duplicate);
    }
}