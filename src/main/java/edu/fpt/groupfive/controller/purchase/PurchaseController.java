package edu.fpt.groupfive.controller.purchase;

import edu.fpt.groupfive.common.Request;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/asset-manager")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final AssetTypeService assetTypeService;
    private final UserService userService;

    // hiển thị form tạo purchase mới
    @GetMapping("/create/purchase-form")
    public String showPurchaseForm(Model model) {

        PurchaseCreateRequest purchaseCreateRequest = new PurchaseCreateRequest();
        purchaseCreateRequest.getPurchaseDetailCreateRequests().add(new PurchaseDetailCreateRequest());

        model.addAttribute("purchaseCreateRequest", purchaseCreateRequest);

        getAssetType(model);
        ActiveNavbar(model);
        return "purchase/purchase-form";
    }

    // hiển thị form sửa purchase draft
    @GetMapping("/purchases/{id}/edit")
    public String showEditPurchaseForm(@PathVariable("id") Integer id, Model model) {

        PurchaseCreateRequest purchaseCreateRequest = purchaseService.loadDraftForEdit(id);

        model.addAttribute("purchaseCreateRequest", purchaseCreateRequest);

        getAssetType(model);
        ActiveNavbar(model);
        return "purchase/purchase-form";
    }

    private static void ActiveNavbar(Model model) {
        model.addAttribute("activeMenu", "approval");
        model.addAttribute("activeSub", "pr");
    }

    // purchase detail
    @GetMapping("/show/purchase-detail/{purchaseId}")
    public String showPurchaseDetail(@PathVariable("purchaseId") Integer purchaseId, Model model) {
        model.addAttribute("purchase", purchaseService.findById(purchaseId));
        ActiveNavbar(model);
        return "purchase/purchase-detail";
    }

    // them 1 row ở purchase dertail chỗ form nhập
    @PostMapping(value = "/create/purchase-form", params = "addDetail")
    public String addPurchaseDetail(
            @ModelAttribute("purchaseCreateRequest") PurchaseCreateRequest purchaseCreateRequest, Model model) {

        // tạo thêm 1 dòng detail mới
        purchaseCreateRequest.getPurchaseDetailCreateRequests().add(new PurchaseDetailCreateRequest());
        getAssetType(model);
        ActiveNavbar(model);
        return "purchase/purchase-form";
    }

    // xóa đi 1 row
    @PostMapping(value = "/create/purchase-form", params = "remove")
    public String removePurchaseDetail(
            @ModelAttribute("purchaseCreateRequest") PurchaseCreateRequest purchaseCreateRequest,
            @RequestParam("remove") int index, Model model) {

        // check điều kiện để xóa đi 1 dòng detail
        if (index >= 0 && purchaseCreateRequest.getPurchaseDetailCreateRequests().size() > 1) {
            purchaseCreateRequest.getPurchaseDetailCreateRequests().remove(index);
        }
        getAssetType(model);
        ActiveNavbar(model);
        return "purchase/purchase-form";
    }

    // xử lí form nhập
    @PostMapping(value = "/create/purchase-form", params = "actions")
    public String processingForm(
            @Valid @ModelAttribute("purchaseCreateRequest") PurchaseCreateRequest purchaseCreateRequest,
            BindingResult result,
            Model model,
            @RequestParam("actions") String actions) {

        boolean isDraft = "draft".equals(actions);

        // có lỗi thì return
        if (result.hasErrors()) {
            getAssetType(model);
            ActiveNavbar(model);
            return "purchase/purchase-form";
        }

        // lấy ra user đang login
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int userId = userService.getUserIdByUsername(authentication.getName());

        Integer purchaseId;
        if (isDraft) {
            purchaseId = purchaseService.createPurchaseRequest(purchaseCreateRequest, userId, Request.DRAFT);
        } else {
            purchaseId = purchaseService.createPurchaseRequest(purchaseCreateRequest, userId, Request.PENDING);
        }
        return "redirect:/asset-manager/purchase-detail/" + purchaseId;
    }

    // gắn assetType
    private void getAssetType(Model model) {
        List<AssetTypeResponse> assetTypes = assetTypeService.getAllAssetType();
        model.addAttribute("assetTypes", assetTypes);
    }

}