package edu.fpt.groupfive.controller.purchase;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dto.request.PurchaseRequestCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseRequestDetailCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseRequestSearchCriteria;
import edu.fpt.groupfive.dto.response.PurchaseRequestResponse;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.PurchaseService;
import edu.fpt.groupfive.service.UserService;
import edu.fpt.groupfive.util.annotation.IsAssetManager;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
@RequestMapping("/purchases")
public class PurchaseController {

    private static final String URL_PURCHASE_LIST = "purchase/purchase-list";
    private static final String URL_PURCHASE_FORM = "purchase/purchase-form";

    @Value("${purchase.noti.create.successfully}")
    private String messageSuccess;

    @Value("${purchase.success.update}")
    private String messageActions;

    // khởi tọa các dependency
    private final PurchaseService purchaseService;
    private final AssetTypeService assetTypeService;
    private final UserService userService;

    // bind opject
    @ModelAttribute("searchAndFilter")
    public PurchaseRequestSearchCriteria initSearchAndFilter() {
        return new PurchaseRequestSearchCriteria();
    }

    // hiển thị danh sách purchase request
    @GetMapping("")
    public String showPurchases(Model model) {
        model.addAttribute("purchases", purchaseService.getPurchaseRequests());
        prepareFilter(model);
        return URL_PURCHASE_LIST;
    }

    // search and filter cho màn purchase list
    @GetMapping("/search")
    public String searchPurchases(@ModelAttribute("searchAndFilter") PurchaseRequestSearchCriteria criteria,
            Model model) {

        List<PurchaseRequestResponse> purchaseRequestResponses = List.of();

        try {
            purchaseRequestResponses = purchaseService.searchPurchaseRequests(criteria);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        model.addAttribute("purchases", purchaseRequestResponses);
        prepareFilter(model);

        return URL_PURCHASE_LIST;
    }

    // hiển thị form tạo purchase mới
    @IsAssetManager
    @GetMapping("/create")
    public String showPurchaseForm(Model model) {

        // thêm sẵn 1 dòng detail trước.
        PurchaseRequestCreateRequest purchaseCreateRequest = new PurchaseRequestCreateRequest();
        purchaseCreateRequest.getPurchaseRequestDetailCreateRequests().add(new PurchaseRequestDetailCreateRequest());
        model.addAttribute("purchaseCreateRequest", purchaseCreateRequest);
        prepareFormModel(model);
        return URL_PURCHASE_FORM;
    }

    // hiển thị form sửa purchase ruquest
    @IsAssetManager
    @GetMapping("/{id}/edit")
    public String showEditPurchaseForm(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {

        try {
            PurchaseRequestCreateRequest p = purchaseService.preparePurchaseRequestForm(id);
             model.addAttribute("purchaseCreateRequest", p);
         prepareFormModel(model);
            return URL_PURCHASE_FORM;
        }catch (InvalidDataException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/purchases";

        }
    }

    // hiển thị chi tiết purchase request
    @GetMapping("/{purchaseId}")
    public String showPurchaseDetail(@PathVariable("purchaseId") Integer purchaseId, Model model) {
        model.addAttribute("purchase", purchaseService.getPurchaseRequestById(purchaseId));
        setNavbar(model);
        return "purchase/purchase-detail";
    }

    // duyệt và từ chối purhcase reuqest
    @PostMapping("/{id}/actions")
    public String actionWithPr(@PathVariable("id") Integer id,
            @RequestParam("action") String actions,
            @RequestParam(value = "reasonReject", required = false) String reasonReject,
                               RedirectAttributes redirectAttributes) {

        try{
        purchaseService.processPurchaseRequestAction(id, actions, reasonReject, getCurrentUserId());
        redirectAttributes.addFlashAttribute("message", messageActions);
        }catch (InvalidDataException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/purchases";
    }

    // Thêm 1 dòng detail trong form
    @IsAssetManager
    @PostMapping(value = "/create", params = "addDetail")
    public String addPurchaseDetail(
            @ModelAttribute("purchaseCreateRequest") PurchaseRequestCreateRequest purchaseCreateRequest, Model model) {
        purchaseCreateRequest.getPurchaseRequestDetailCreateRequests().add(new PurchaseRequestDetailCreateRequest());
        prepareFormModel(model);
        return URL_PURCHASE_FORM;
    }

    // Xóa 1 dòng detail trong form
    @IsAssetManager
    @PostMapping(value = "/create", params = "remove")
    public String removePurchaseDetail(
            @ModelAttribute("purchaseCreateRequest") PurchaseRequestCreateRequest purchaseCreateRequest,
            @RequestParam("remove") int index, Model model) {

        // chỉ index > 0 thì mới có thể xóa
        if (index >= 0 && purchaseCreateRequest.getPurchaseRequestDetailCreateRequests().size() > 1) {
            purchaseCreateRequest.getPurchaseRequestDetailCreateRequests().remove(index);
        }

        prepareFormModel(model);
        return URL_PURCHASE_FORM;
    }

    // xử lí form tạo purchase request
    @IsAssetManager
    @PostMapping(value = "/create", params = "actions")
    public String processingForm(
            @Valid @ModelAttribute("purchaseCreateRequest") PurchaseRequestCreateRequest purchaseCreateRequest,
            BindingResult result,
            Model model,
            @RequestParam("actions") String actions,
            RedirectAttributes redirectAttributes) {

        // trả lại nếu có lỗi
        if (result.hasErrors()) {
            prepareFormModel(model);
            return URL_PURCHASE_FORM;
        }

        // check là draft hay pending
        boolean isDraft = "draft".equals(actions);
        Request status = isDraft ? Request.DRAFT : Request.PENDING;

        // gọi service lưu request
        Integer purchaseId = purchaseService.createPurchaseRequest(purchaseCreateRequest, getCurrentUserId(), status);

        // nếu tạo thành công
        if (purchaseId != null && purchaseId > 0) {
            redirectAttributes.addFlashAttribute("message", messageSuccess);
        }
        return "redirect:/purchases/" + purchaseId;
    }

    // gán asset type
    private void prepareFormModel(Model model) {
        model.addAttribute("assetTypes", assetTypeService.getAll());
        setNavbar(model);
    }

    // set các field cho filter
    private void prepareFilter(Model model) {
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("status", Request.values());
        setNavbar(model);
    }

    // hiển thị navbar
    private void setNavbar(Model model) {
        model.addAttribute("activeMenu", "approval");
        model.addAttribute("activeSub", "pr");
    }

    // lấy ra use đang login hiện tại
    private Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.getUserIdByUsername(auth.getName());
    }
}
