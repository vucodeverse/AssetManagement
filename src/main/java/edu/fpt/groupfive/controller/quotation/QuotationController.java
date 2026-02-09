package edu.fpt.groupfive.controller.quotation;

import edu.fpt.groupfive.dto.request.PurchaseCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseDetailCreateRequest;
import edu.fpt.groupfive.dto.request.QuotationCreateDetailRequest;
import edu.fpt.groupfive.dto.request.QuotationCreateRequest;
import edu.fpt.groupfive.service.QuotationService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j(topic = "CONTROLLER-QUOTATION[]")
@Controller
@RequestMapping("/purchase-staff")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService quotationService;

    @GetMapping("/purchases/{purchaseId}/quotation-form")
    public String showQuotationForm(@PathVariable Integer purchaseId, Model model){
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
        return "quotation/quotation-form";
    }

    @PostMapping("/purchases/{purchaseId}/quotation")
    public String createQuotation(@PathVariable Integer purchaseId,
            @ModelAttribute("quotationCreateRequest") QuotationCreateRequest quotationCreateRequest, BindingResult bindingResult, Model model){
        if(bindingResult.hasErrors()){
            return "quotation/quotation-form";
        }

        quotationService.createQuotation(purchaseId,quotationCreateRequest);

        return "";
    }

}
