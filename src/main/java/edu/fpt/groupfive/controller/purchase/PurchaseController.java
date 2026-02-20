package edu.fpt.groupfive.controller.purchase;


import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dto.request.PurchaseCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseDetailCreateRequest;
import edu.fpt.groupfive.dto.response.AssetTypeResponse;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.PurchaseService;
import edu.fpt.groupfive.service.UserService;
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


    // hiển thị form nhập
    @GetMapping("/purchase-form")
    public String showPurchaseForm(Model model){
        PurchaseCreateRequest purchaseCreateRequest = new PurchaseCreateRequest();
        purchaseCreateRequest.getPurchaseDetailCreateRequests().add(new PurchaseDetailCreateRequest());
        model.addAttribute("purchaseCreateRequest", purchaseCreateRequest);
        getAssetType(model);
        return "purchase/purchase-form";
    }


    // show detail - creatorName và assetTypeName đã được JOIN sẵn ở DAO, không cần gửi users/assetTypes lên
    @GetMapping("/purchase-detail/{purchaseId}")
    public String showPurchaseDetail(@PathVariable("purchaseId") Integer purchaseId, Model model) {
        model.addAttribute("purchase", purchaseService.findById(purchaseId));
        return "purchase/purchase-detail";
    }


    // khi add 1 purchase detail mới
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

    // xử lí form nhập
    @PostMapping(value = "/purchase-form", params = "actions")
    public String processingForm( @ModelAttribute("purchaseCreateRequest") PurchaseCreateRequest purchaseCreateRequest, BindingResult result,Model model, @RequestParam("actions") String actions){
        if(result.hasErrors()){
            getAssetType(model);
            return "purchase/purchase-form";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // nếu save draft
        if("draft".equals(actions))
            purchaseService.createPurchaseRequest(purchaseCreateRequest,
                    userService.getUserIdByUsername(authentication.getName()), Request.DRAFT);

        else
            purchaseService.createPurchaseRequest(purchaseCreateRequest,
                userService.getUserIdByUsername(authentication.getName()), Request.PENDING);

        return "purchase/purchase-form";
    }


    // lấy ra tất cả các asset type (chỉ dùng cho purchase-form, không dùng cho detail nữa)
    private void getAssetType(Model model) {
        List<AssetTypeResponse> assetTypes = assetTypeService.getAllAssetType();
        model.addAttribute("assetTypes", assetTypes);
    }


}
