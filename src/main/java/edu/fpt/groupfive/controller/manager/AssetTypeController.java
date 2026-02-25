package edu.fpt.groupfive.controller.manager;

import edu.fpt.groupfive.common.AssetTypeClass;
import edu.fpt.groupfive.common.DepreciationMethod;
import edu.fpt.groupfive.dto.request.AssetTypeCreateRequest;
import edu.fpt.groupfive.dto.request.AssetTypeUpdateRequest;
import edu.fpt.groupfive.dto.response.AssetTypeResponse;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.CategoryService;

import edu.fpt.groupfive.util.exception.InvalidDataException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("manager/asset-types")
@RequiredArgsConstructor
public class AssetTypeController {

    private final AssetTypeService assetTypeService;
    private final CategoryService categoryService;

    // VIEW
    @GetMapping
    public String viewPage(Model model) {


        model.addAttribute("assetType", new AssetTypeCreateRequest());
        model.addAttribute("mode", "create");
        loadCommonData(model);

        return "manager/asset-type-page";
    }

    // CREATE
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("assetType") AssetTypeCreateRequest request,
                         BindingResult bindingResult,
                         Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "create");
            loadCommonData(model);
            return "manager/asset-type-page";
        }
        try {
            assetTypeService.create(request);
            return "redirect:/manager/asset-types";
        } catch (InvalidDataException e) {
            model.addAttribute("mode", "create");
            model.addAttribute("errorMessage", e.getMessage()
            );
            loadCommonData(model);
            return "manager/asset-type-page";
        }

    }

    // EDIT FORM
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model) {

        AssetTypeResponse response = assetTypeService.getById(id);

        AssetTypeUpdateRequest updateRequest = new AssetTypeUpdateRequest();
        updateRequest.setTypeId(response.getTypeId());
        updateRequest.setTypeName(response.getTypeName());
        updateRequest.setTypeClass(response.getTypeClass());
        updateRequest.setDescription(response.getDescription());
        updateRequest.setSpecification(response.getSpecification());
        updateRequest.setModel(response.getModel());
        updateRequest.setDefaultDepreciationMethod(response.getDefaultDepreciationMethod());
        updateRequest.setDefaultUsefulLifeMonths(response.getDefaultUsefulLifeMonths());
        updateRequest.setCategoryId(response.getCategoryId());


        model.addAttribute("assetType", updateRequest);
        model.addAttribute("mode", "update");

        loadCommonData(model);

        return "manager/asset-type-page";
    }

    // UPDATE
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("assetType") AssetTypeUpdateRequest request,
                         BindingResult bindingResult,
                         Model model) {

        if (bindingResult.hasErrors()) {


            model.addAttribute("mode", "update");
            loadCommonData(model);
            return "manager/asset-type-page";
        }
        try {
            assetTypeService.update(request);
            return "redirect:/manager/asset-types";
        } catch (InvalidDataException ex) {

            model.addAttribute("mode", "update");
            model.addAttribute("errorMessage", ex.getMessage());

            loadCommonData(model);
            return "manager/asset-type-page";
        }
    }

    // DELETE
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {

        try {
            assetTypeService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa thành công!");

        } catch (InvalidDataException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/manager/asset-types";
    }


    private void loadCommonData(Model model) {
        model.addAttribute("assetTypes", assetTypeService.getAll());
        model.addAttribute("typeClasses", AssetTypeClass.values());
        model.addAttribute("depreciationMethods", DepreciationMethod.values());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("active", "assetType");
    }
}