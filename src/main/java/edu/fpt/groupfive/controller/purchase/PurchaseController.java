package edu.fpt.groupfive.controller.purchase;


import edu.fpt.groupfive.dto.request.PurchaseCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseDetailCreateRequest;
import edu.fpt.groupfive.dto.response.AssetTypeResponse;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.PurchaseService;
import edu.fpt.groupfive.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j(topic = "PURCHASE-CONTROLLER")
@RequestMapping("/asset-manager")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final AssetTypeService assetTypeService;
    private final UserService userService;

    @GetMapping("/purchase-form")
    public String showPurchaseForm(Model model){
        log.info("Show form purchase request");

        // tạo bind object
        PurchaseCreateRequest purchaseCreateRequest = new PurchaseCreateRequest();
        purchaseCreateRequest.getPurchaseDetailCreateRequests().add(new PurchaseDetailCreateRequest());

        getAssetType(model);
        model.addAttribute("purchaseCreateRequest", purchaseCreateRequest);
        return "purchase/purchase-form";
    }


    // khi add 1 purchase detail mowis
    @PostMapping(value = "/purchase-form", params = "addDetail")
    public String addPurchaseDetail(@ModelAttribute("purchaseCreateRequest") PurchaseCreateRequest purchaseCreateRequest, Model model){
        purchaseCreateRequest.getPurchaseDetailCreateRequests().add(new PurchaseDetailCreateRequest());
        getAssetType(model);
        return "purchase/purchase-form";
    }

    // khi xoa 1 purchase detail di
    @PostMapping(value = "/purchase-form", params = "remove")
    public String removePurchaseDetail(@ModelAttribute("purchaseCreateRequest") PurchaseCreateRequest purchaseCreateRequest, @RequestParam("remove") int index, Model model){
        if(index >= 0 && purchaseCreateRequest.getPurchaseDetailCreateRequests().size() > 1){
            purchaseCreateRequest.getPurchaseDetailCreateRequests().remove(index);
        }
        getAssetType(model);
        return "purchase/purchase-form";
    }

    @PostMapping("/purchase-form")
    public String processingForm(@Valid @ModelAttribute("purchaseCreateRequest") PurchaseCreateRequest purchaseCreateRequest, BindingResult result,
                                 Model model){
        if(result.hasErrors()){
            getAssetType(model);
            return "purchase/purchase-form";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        purchaseService.createPurchaseRequest(purchaseCreateRequest,userService.getUserIdByUsername(authentication.getName()));

        return "redirect/:purchase/purchase-form";
    }
    private void getAssetType(Model model) {
        List<AssetTypeResponse> assetTypes = assetTypeService.getAllAssetType();
        model.addAttribute("assetTypes", assetTypes);
    }

}
