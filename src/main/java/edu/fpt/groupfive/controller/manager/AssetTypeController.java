package edu.fpt.groupfive.controller.manager;

import edu.fpt.groupfive.common.AssetTypeClass;
import edu.fpt.groupfive.common.DepreciationMethod;
import edu.fpt.groupfive.dto.request.AssetTypeCreateRequest;
import edu.fpt.groupfive.dto.request.AssetTypeUpdateRequest;
import edu.fpt.groupfive.dto.response.AssetTypeResponse;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("manager/asset-types")
@RequiredArgsConstructor
public class AssetTypeController {

    private final AssetTypeService assetTypeService;
    private final CategoryService categoryService;

    // ================== VIEW ==================
    @GetMapping
    public String viewPage(Model model) {

        model.addAttribute("assetTypes", assetTypeService.getAll());
        model.addAttribute("assetType", new AssetTypeCreateRequest());
        model.addAttribute("mode", "create");

        model.addAttribute("typeClasses", AssetTypeClass.values());
        model.addAttribute("depreciationMethods", DepreciationMethod.values());
        model.addAttribute("categories", categoryService.getAll());

        return "manager/asset-type-page";
    }

    // ================== CREATE ==================
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("assetType") AssetTypeCreateRequest request,
                         BindingResult bindingResult,
                         Model model) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("assetTypes", assetTypeService.getAll());
            model.addAttribute("mode", "create");

            model.addAttribute("typeClasses", AssetTypeClass.values());
            model.addAttribute("depreciationMethods", DepreciationMethod.values());
            model.addAttribute("categories", categoryService.getAll());

            return "manager/asset-type-page";
        }

        assetTypeService.create(request);
        return "redirect:/manager/asset-types";
    }

    // ================== EDIT FORM ==================
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {

        AssetTypeResponse response = assetTypeService.getById(id);

        AssetTypeUpdateRequest updateRequest = new AssetTypeUpdateRequest();
        updateRequest.setTypeId(response.getTypeId());
        updateRequest.setTypeName(response.getTypeName());
        updateRequest.setTypeClass(response.getTypeClass());
        updateRequest.setDefaultDepreciationMethod(response.getDefaultDepreciationMethod());
        updateRequest.setDefaultUsefulLifeMonths(response.getDefaultUsefulLifeMonths());
        updateRequest.setCategoryId(response.getCategoryId());

        model.addAttribute("assetTypes", assetTypeService.getAll());
        model.addAttribute("assetType", updateRequest);
        model.addAttribute("mode", "update");

        model.addAttribute("typeClasses", AssetTypeClass.values());
        model.addAttribute("depreciationMethods", DepreciationMethod.values());
        model.addAttribute("categories", categoryService.getAll());

        return "manager/asset-type-page";
    }

    // ================== UPDATE ==================
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("assetType") AssetTypeUpdateRequest request,
                         BindingResult bindingResult,
                         Model model) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("assetTypes", assetTypeService.getAll());
            model.addAttribute("mode", "update");

            model.addAttribute("typeClasses", AssetTypeClass.values());
            model.addAttribute("depreciationMethods", DepreciationMethod.values());
            model.addAttribute("categories", categoryService.getAll());

            return "manager/asset-type-page";
        }

        assetTypeService.update(request);
        return "redirect:/manager/asset-types";
    }

    // ================== DELETE ==================
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {

        assetTypeService.delete(id);
        return "redirect:/manager/asset-types";
    }
}