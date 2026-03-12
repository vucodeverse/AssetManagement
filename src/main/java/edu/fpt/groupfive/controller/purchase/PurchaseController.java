package edu.fpt.groupfive.controller.purchase;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dto.request.PurchaseRequestCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseRequestDetailCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseRequestSearchCriteria;
import edu.fpt.groupfive.dto.response.AssetTypeResponse;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.PurchaseService;
import edu.fpt.groupfive.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/purchase-staff/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final AssetTypeService assetTypeService;
    private final UserService userService;

    // mặc định khởi tạo
    @ModelAttribute("searchAndFilter")
    public PurchaseRequestSearchCriteria initSearchAndFilter() {
        return new PurchaseRequestSearchCriteria();
    }

    // hiển thị danh sách yêu cầu mua sắm cho staff
    @GetMapping("")
    public String showPurchases(Model model) {
        model.addAttribute("activeMenu", "approval");
        model.addAttribute("activeSub", "pr");
        model.addAttribute("purchases", purchaseService.findAllPurchases());
        addFilterAttributes(model);
        return "purchase/purchase-list";
    }

    // search and filter và lọc yêu cầu mua sắm
    @GetMapping("/search")
    public String searchPurchases(@ModelAttribute("searchAndFilter") PurchaseRequestSearchCriteria criteria,
            Model model) {
        model.addAttribute("activeMenu", "approval");
        model.addAttribute("activeSub", "pr");
        model.addAttribute("purchases", purchaseService.searchAndFilter(criteria));
        addFilterAttributes(model);
        return "purchase/purchase-list";
    }

    // hiển thị form tạo purchase mới
    @GetMapping("/create")
    public String showPurchaseForm(Model model) {

        PurchaseRequestCreateRequest purchaseCreateRequest = new PurchaseRequestCreateRequest();
        purchaseCreateRequest.getPurchaseRequestDetailCreateRequests().add(new PurchaseRequestDetailCreateRequest());

        model.addAttribute("purchaseCreateRequest", purchaseCreateRequest);
        addFilterAttributes(model);
        getAssetType(model);
        ActiveNavbar(model);
        return "purchase/purchase-form";
    }

    // hiển thị form sửa purchase khi update draft
    @GetMapping("/{id}/edit")
    public String showEditPurchaseForm(@PathVariable("id") Integer id, Model model) {

        PurchaseRequestCreateRequest purchaseCreateRequest = purchaseService.loadDraftForEdit(id);

        model.addAttribute("purchaseCreateRequest", purchaseCreateRequest);

        getAssetType(model);
        ActiveNavbar(model);
        return "purchase/purchase-form";
    }


    // Chi tiết yêu cầu mua sắm
    @GetMapping("/{purchaseId}")
    public String showPurchaseDetail(@PathVariable("purchaseId") Integer purchaseId, Model model) {
        model.addAttribute("purchase", purchaseService.findById(purchaseId));
        ActiveNavbar(model);
        return "purchase/purchase-detail";
    }

    // them 1 row ở purchase dertail chỗ form nhập
    @PostMapping(value = "/create", params = "addDetail")
    public String addPurchaseDetail(
            @ModelAttribute("purchaseCreateRequest") PurchaseRequestCreateRequest purchaseCreateRequest, Model model) {

        // tạo thêm 1 dòng detail mới
        purchaseCreateRequest.getPurchaseRequestDetailCreateRequests().add(new PurchaseRequestDetailCreateRequest());
        getAssetType(model);
        ActiveNavbar(model);
        return "purchase/purchase-form";
    }

    // xóa đi 1 row
    @PostMapping(value = "/create", params = "remove")
    public String removePurchaseDetail(
            @ModelAttribute("purchaseCreateRequest") PurchaseRequestCreateRequest purchaseCreateRequest,
            @RequestParam("remove") int index, Model model) {

        // check điều kiện để xóa đi 1 dòng detail
        if (index >= 0 && purchaseCreateRequest.getPurchaseRequestDetailCreateRequests().size() > 1) {
            purchaseCreateRequest.getPurchaseRequestDetailCreateRequests().remove(index);
        }
        getAssetType(model);
        ActiveNavbar(model);
        return "purchase/purchase-form";
    }

    // xử lí form nhập
    @PostMapping(value = "/create", params = "actions")
    public String processingForm(
            @Valid @ModelAttribute("purchaseCreateRequest") PurchaseRequestCreateRequest purchaseCreateRequest,
            BindingResult result,
            Model model,
            @RequestParam("actions") String actions) {

        // check xem là draft hay save
        boolean isDraft = "draft".equals(actions);

        // có lỗi thì return
        if (result.hasErrors()) {
            getAssetType(model);
            ActiveNavbar(model);
            return "purchase/purchase-form";
        }

        // TODO: Chưa bật bảo mật, tạm thời để userId = 2
        // Authentication authentication =
        // SecurityContextHolder.getContext().getAuthentication();
        // int userId = userService.getUserIdByUsername(authentication.getName());
        int userId = 2;

        Integer purchaseId;
        if (isDraft) {
            purchaseId = purchaseService.createPurchaseRequest(purchaseCreateRequest, userId, Request.DRAFT);
        } else {
            purchaseId = purchaseService.createPurchaseRequest(purchaseCreateRequest, userId, Request.PENDING);
        }
        return "redirect:/purchase-staff/purchases/" + purchaseId;
    }

    // gắn assetType
    private void getAssetType(Model model) {
        List<AssetTypeResponse> assetTypes = assetTypeService.getAll();
        model.addAttribute("assetTypes", assetTypes);
    }

    // add các common
    private void addFilterAttributes(Model model) {
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("status", Request.values());
    }

    // hiện navbar
    private static void ActiveNavbar(Model model) {
        model.addAttribute("activeMenu", "approval");
        model.addAttribute("activeSub", "pr");
    }
}
