package edu.fpt.groupfive.controller.quotation;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.common.QuotationStatus;
import edu.fpt.groupfive.dto.request.*;
import edu.fpt.groupfive.dto.response.QuotationForPurchaseResponse;
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
import java.math.BigDecimal;
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


        model.addAttribute("quotationCreateRequest", quotationCreateRequest);
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("purchaseId", purchaseId);
        return "quotation/quotation-form";
    }





    @PostMapping(value = "/purchases/{purchaseId}/quotation", params = "addDetail")
    public String addQuotationDetail(@PathVariable("purchaseId") Integer purchaseId,
                                     @ModelAttribute("quotationCreateRequest") QuotationCreateRequest quotationCreateRequest,
                                     @RequestParam("addDetail") int index,
                                     Model model) {
        if (index >= 0 && index < quotationCreateRequest.getQuotationCreateDetailRequestList().size()) {
            QuotationCreateDetailRequest originalItem = quotationCreateRequest.getQuotationCreateDetailRequestList().get(index);
            
            // Create a copy of the item
            QuotationCreateDetailRequest newItem = new QuotationCreateDetailRequest();
            newItem.setPurchaseRequestDetailId(originalItem.getPurchaseRequestDetailId());
            newItem.setQuantity(originalItem.getQuantity());
            newItem.setAssetTypeName(originalItem.getAssetTypeName()); // Copy display info
            newItem.setSpecificationRequirement(originalItem.getSpecificationRequirement()); // Copy display info
            
            // Insert after the current item
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


    // thêm dòng quotation detail
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

    // show list quotation của pchase
    @GetMapping("/purchases/{purchaseId}/quotation-list")
    public String showQuotationForPurchase(@PathVariable("purchaseId") Integer purchaseId , Model model){
        List<QuotationResponse> quotations = quotationService.getQuotationsByPurchase(purchaseId);
        model.addAttribute("quotations", quotations);
        model.addAttribute("isDetailView", true);
        model.addAttribute("purchaseId", purchaseId);
        model.addAttribute("searchForQuotation", new SearchForQuotation());
        model.addAttribute("priorities", Priority.values());
        return "quotation/quotation-list";
    }

    // hiển thị list quotaiton theo purchase id
    @PostMapping("/view-quotation-of-purchase/{purchaseId}")
    public String viewQuotationListPost(@PathVariable("purchaseId") Integer purchaseId, Model model) {
        List<QuotationResponse> quotations = quotationService.getQuotationsByPurchase(purchaseId);

        model.addAttribute("statuses", QuotationStatus.values());
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("activeSub", "qt");
        model.addAttribute("activeMenu", "approval");

        return "quotation/quotation-of-purchase";
    }


    // show detail
    @GetMapping("/quotations/{id}")
    public String getQuotation(@PathVariable("id") Integer id, Model model) {
        QuotationResponse quotation = quotationService.getQuotationById(id);
        model.addAttribute("quotation", quotation);
        return "quotation/quotation-detail";
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


    // khởi tạo
    @ModelAttribute("searchForQuotation")
    public SearchForQuotation initSearchForQuotation() {
        return new SearchForQuotation();
    }
}
