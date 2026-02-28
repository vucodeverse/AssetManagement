package edu.fpt.groupfive.controller.manager;

import edu.fpt.groupfive.dto.request.AssetCreateRequest;
import edu.fpt.groupfive.dto.request.AssetUpdateRequest;
import edu.fpt.groupfive.dto.response.AssetResponse;
import edu.fpt.groupfive.service.AssetService;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager/assets")
public class AssetController {

    private final AssetService assetService;
    private final AssetTypeService assetTypeService;

    @GetMapping
    public String list(Model model) {
        List<AssetResponse> assets = assetService.getAll();
        model.addAttribute("assets", assets);
        return "manager/asset/asset-list";
    }


    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Integer id, Model model) {
        try {
            AssetResponse asset = assetService.getById(id);
            model.addAttribute("asset", asset);
            return "manager/asset/asset-detail";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return list(model);
        }
    }


    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("asset", new AssetCreateRequest());
        model.addAttribute("assetTypes", assetTypeService.getAll());
        model.addAttribute("isEdit", false);
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
            model.addAttribute("isEdit", false);
            return "manager/asset/asset-form";
        }

        try {
            assetService.create(request);
        } catch (InvalidDataException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("assetTypes", assetTypeService.getAll());
            model.addAttribute("isEdit", false);
            return "manager/asset/asset-form";
        }

        return "redirect:/manager/assets";
    }

    // =========================
    // EDIT FORM
    // =========================
    @GetMapping("/edit/{id}")
    public String showEdit(@PathVariable("id") Integer id, Model model) {

        try {
            AssetResponse asset = assetService.getById(id);

            AssetUpdateRequest request = new AssetUpdateRequest();
            request.setAssetId(asset.getAssetId());
            request.setSerialNumber(asset.getSerialNumber());
            request.setCurrentStatus(asset.getCurrentStatus());
            request.setWarrantyStartDate(asset.getWarrantyStartDate());
            request.setWarrantyEndDate(asset.getWarrantyEndDate());
            request.setOriginalCost(asset.getOriginalCost());
            request.setAssetTypeId(asset.getAssetTypeId());
            request.setAcquisitionDate(asset.getAcquisitionDate());

            model.addAttribute("asset", request);
            model.addAttribute("assetTypes", assetTypeService.getAll());
            model.addAttribute("isEdit", true);

            return "manager/asset/asset-form";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return list(model);
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
            model.addAttribute("isEdit", true);
            return "manager/asset/asset-form";
        }

        try {
            request.setAssetId(id);
            assetService.update(id, request);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("assetTypes", assetTypeService.getAll());
            model.addAttribute("isEdit", true);
            return "manager/asset/asset-form";
        }

        return "redirect:/manager/assets";
    }


    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        assetService.delete(id);
        return "redirect:/manager/assets";
    }
}