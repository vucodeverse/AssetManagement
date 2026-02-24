package edu.fpt.groupfive.controller.quotation;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.common.QuotationStatus;
import edu.fpt.groupfive.dto.request.*;
import edu.fpt.groupfive.dto.response.QuotationResponse;
import edu.fpt.groupfive.service.QuotationService;
import edu.fpt.groupfive.service.SupplierService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j(topic = "CONTROLLER-QUOTATION[]")
@Controller
@RequestMapping("/purchase-staff")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService quotationService;
    private final SupplierService supplierService;

    // show form add
    @GetMapping("/purchases/{purchaseId}/quotation-form")
    public String showQuotationForm(@PathVariable("purchaseId") Integer purchaseId, Model model){
        log.info("Load form quotation");

        // ktra va load quotation create lên
        QuotationCreateRequest quotationCreateRequest = new QuotationCreateRequest();
        try {
             quotationCreateRequest = quotationService.checkFormQuotation(purchaseId);
        }catch (InvalidDataException e){
            model.addAttribute("error", e.getMessage());
            return "purchase/purchase-home";
        }

        // mặc định cho sẵn 1 dòng detail
        model.addAttribute("quotationCreateRequest", quotationCreateRequest);
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("purchaseId", purchaseId);
        return "quotation/quotation-form";
    }

    // thêm 1 dòng mời
    @PostMapping(value = "/purchases/{purchaseId}/quotation", params = "addDetail")
    public String addQuotationDetail(@PathVariable("purchaseId") Integer purchaseId,
                                     @ModelAttribute("quotationCreateRequest") QuotationCreateRequest quotationCreateRequest,
                                     @RequestParam("addDetail") int index,
                                     Model model) {
        if (index >= 0 && index < quotationCreateRequest.getQuotationCreateDetailRequestList().size()) {
            QuotationCreateDetailRequest quotationCreateDetailRequest = quotationCreateRequest.getQuotationCreateDetailRequestList().get(index);
            
            // add item mưới
            // add các data sẵn
            QuotationCreateDetailRequest newItem = new QuotationCreateDetailRequest();
            newItem.setPurchaseRequestDetailId(quotationCreateDetailRequest.getPurchaseRequestDetailId());
            newItem.setQuantity(quotationCreateDetailRequest.getQuantity());
            newItem.setAssetTypeName(quotationCreateDetailRequest.getAssetTypeName());
            newItem.setSpecificationRequirement(quotationCreateDetailRequest.getSpecificationRequirement());

            // thêm 1 dòng sau khi add itme
            quotationCreateRequest.getQuotationCreateDetailRequestList().add(index + 1, newItem);
        }


        model.addAttribute("quotationCreateRequest", quotationCreateRequest);
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("purchaseId", purchaseId);
        return "quotation/quotation-form";
    }


    // xóa 1 dòng quoationdetail
    @PostMapping(value = "/purchases/{purchaseId}/quotation", params = "removeDetail")
    public String removeQuotationDetail(@PathVariable("purchaseId") Integer purchaseId,
                                        @ModelAttribute("quotationCreateRequest") QuotationCreateRequest quotationCreateRequest,
                                        @RequestParam("removeDetail") int index,
                                        Model model) {
        if (index >= 0 && index < quotationCreateRequest.getQuotationCreateDetailRequestList().size()) {
            quotationCreateRequest.getQuotationCreateDetailRequestList().remove(index);
        }

        model.addAttribute("quotationCreateRequest", quotationCreateRequest);
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("purchaseId", purchaseId);
        return "quotation/quotation-form";
    }

    // xử lí form
    @PostMapping("/purchases/{purchaseId}/quotation")
    public String createQuotation(@PathVariable("purchaseId") Integer purchaseId,
            @ModelAttribute("quotationCreateRequest") QuotationCreateRequest quotationCreateRequest, BindingResult bindingResult, Model model){
        if(bindingResult.hasErrors()){
            model.addAttribute("suppliers", supplierService.getAllSupplier());
            model.addAttribute("purchaseId", purchaseId);
            return "quotation/quotation-form";
        }

        quotationService.createQuotation(purchaseId,quotationCreateRequest);

        return "redirect:/asset-manager/purchase-form"; 
    }

    // show list purchase
    @GetMapping("/view-quotation-of-purchase/{purchaseId}")
    public String viewQuotationList(@PathVariable("purchaseId") Integer purchaseId, Model model) {
        List<QuotationResponse> quotations = quotationService.getQuotationsByPurchase(purchaseId);

        model.addAttribute("statuses", QuotationStatus.values());
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("activeSub", "qt");
        model.addAttribute("activeMenu", "approval");
        model.addAttribute("quotations", quotations);
        model.addAttribute("criteria", new QuotationSearchCriteria());

        return "quotation/quotation-of-purchase";
    }


    // show detail
    @GetMapping("/quotations/{id}")
    public String getQuotation(@PathVariable("id") Integer id, Model model) {
        QuotationResponse quotation = quotationService.getQuotationById(id);
        model.addAttribute("quotation", quotation);
        return "quotation/quotation-detail";
    }

    @GetMapping("/search/quotation-of-purchase/{purchaseId}")
    public String searchQuotationOfPurchase(@PathVariable("purchaseId") Integer purchaseId,
                                            @ModelAttribute("criteria") QuotationSearchCriteria criteria,
                                            Model model) {
        criteria.setPurchaseId(purchaseId);
        
        model.addAttribute("purchaseId", purchaseId);
        model.addAttribute("statuses", QuotationStatus.values());
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("activeSub", "qt");
        model.addAttribute("activeMenu", "approval");
        model.addAttribute("quotations", quotationService.quotationCriteriaForPurchase(criteria));
        
        return "quotation/quotation-of-purchase";
    }

    // search and filter cho quotation
    @GetMapping("/search")
    public String searchQuotation(@ModelAttribute("searchForQuotation") SearchForQuotation searchForQuotation, Model model) {
        model.addAttribute("activeSub", "qt");
        model.addAttribute("activeMenu", "approval");
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("quotations", quotationService.searchAndFilterForQuotation(searchForQuotation));
        return "quotation/quotation-list";
    }


    // show list các quotation theo purchase
    @GetMapping("/quotations")
    public String showQuotations(Model model) {
        model.addAttribute("activeSub", "qt");
        model.addAttribute("activeMenu", "approval");
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
