package edu.fpt.groupfive.controller.manager;

import edu.fpt.groupfive.common.AssetStatus;
import edu.fpt.groupfive.dto.request.AssetCreateRequest;
import edu.fpt.groupfive.dto.request.AssetUpdateRequest;
import edu.fpt.groupfive.dto.response.AssetDetailResponse;
import edu.fpt.groupfive.dto.response.AssetResponse;
import edu.fpt.groupfive.dto.response.PageResponse;
import edu.fpt.groupfive.service.AssetService;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.OrderService;
import edu.fpt.groupfive.service.PurchaseService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager/assets")
public class AssetController {

    private final AssetService assetService;
    private final AssetTypeService assetTypeService;
    private final OrderService orderService;


    @GetMapping
    public String list(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) AssetStatus status,
            @RequestParam(name = "fromDate", required = false) LocalDate fromDate,
            @RequestParam(name = "toDate", required = false) LocalDate toDate,
            @RequestParam(name = "direction", required = false) String direction,
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {

        var result = assetService.searchAssets(keyword, status, fromDate, toDate, direction, page);

        model.addAttribute("assets", result.getData());
        model.addAttribute("page", result);

        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("direction", direction);

        model.addAttribute("activeMenu", "asset");


        return "manager/asset/asset-list";
    }


    @GetMapping("detail/{id}")
    public String detail(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {

            AssetDetailResponse asset = assetService.getDetailById(id);
            model.addAttribute("asset", asset);
            model.addAttribute("activeMenu", "asset");
            return "manager/asset/asset-detail";
        } catch (InvalidDataException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/manager/assets";
        }
    }


    @GetMapping("/create")
    public String showCreateForm(Model model) {

        model.addAttribute("asset", new AssetCreateRequest());
        model.addAttribute("assetTypes", assetTypeService.getAll());
        model.addAttribute("orders", orderService.getAllOrderDetails());
        model.addAttribute("statuses", AssetStatus.values());
        model.addAttribute("isEdit", false);
        model.addAttribute("activeMenu", "asset");
        return "manager/asset/asset-form";
    }


    @PostMapping("/create")
    public String create(
            @Valid @ModelAttribute("asset") AssetCreateRequest request,
            BindingResult result,
            Model model
    ) {

        if (result.hasErrors()) {
            model.addAttribute("assetTypes", assetTypeService.getAll());
            model.addAttribute("orders", orderService.getAllOrderDetails());
            model.addAttribute("statuses", AssetStatus.values());
            model.addAttribute("isEdit", false);
            model.addAttribute("activeMenu", "asset");
            return "manager/asset/asset-form";
        }

        try {
            assetService.create(request);
        } catch (InvalidDataException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("assetTypes", assetTypeService.getAll());
            model.addAttribute("orders", orderService.getAllOrderDetails());
            model.addAttribute("statuses", AssetStatus.values());

            model.addAttribute("isEdit", false);
            model.addAttribute("activeMenu", "asset");
            return "manager/asset/asset-form";
        }

        return "redirect:/manager/assets";
    }

    //edit form
    @GetMapping("/edit/{id}")
    public String showEdit(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {

        try {
            AssetResponse asset = assetService.getById(id);

            AssetUpdateRequest request = new AssetUpdateRequest();

            request.setAssetId(asset.getAssetId());
            request.setAssetName(asset.getAssetName());
            request.setPurchaseOrderDetailId(asset.getPurchaseOrderDetailId());
            request.setWarrantyStartDate(asset.getWarrantyStartDate());
            request.setCurrentStatus(asset.getCurrentStatus());
            request.setWarrantyEndDate(asset.getWarrantyEndDate());
            request.setOriginalCost(asset.getOriginalCost());
            request.setAssetTypeId(asset.getAssetTypeId());
            request.setAcquisitionDate(asset.getAcquisitionDate());

            model.addAttribute("asset", request);
            model.addAttribute("assetTypes", assetTypeService.getAll());
            model.addAttribute("orders", orderService.getAllOrderDetails());
            model.addAttribute("statuses", AssetStatus.values());
            model.addAttribute("isEdit", true);
            model.addAttribute("activeMenu", "asset");
            return "manager/asset/asset-form";

        } catch (InvalidDataException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/manager/assets";
        }
    }


    @PostMapping("/edit/{id}")
    public String update(
            @PathVariable("id") Integer id,
            @Valid @ModelAttribute("asset") AssetUpdateRequest request,
            BindingResult result,
            Model model
    ) {

        if (result.hasErrors()) {
            model.addAttribute("assetTypes", assetTypeService.getAll());
            model.addAttribute("orders", orderService.getAllOrderDetails());
            model.addAttribute("statuses", AssetStatus.values());
            model.addAttribute("isEdit", true);
            model.addAttribute("activeMenu", "asset");
            return "manager/asset/asset-form";
        }

        try {
            request.setAssetId(id);
            assetService.update(id, request);
            return "redirect:/manager/assets";
        } catch (InvalidDataException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("assetTypes", assetTypeService.getAll());
            model.addAttribute("orders", orderService.getAllOrderDetails());
            model.addAttribute("statuses", AssetStatus.values());
            model.addAttribute("isEdit", true);
            model.addAttribute("activeMenu", "asset");

            return "manager/asset/asset-form";
        }
    }


    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id,
                         RedirectAttributes redirectAttributes) {

        try{
            assetService.delete(id);
        } catch (InvalidDataException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

        }
        return "redirect:/manager/assets";
    }


}