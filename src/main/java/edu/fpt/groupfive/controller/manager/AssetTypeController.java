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

import java.util.List;

@Controller
@RequestMapping("manager/asset-types")
@RequiredArgsConstructor
public class AssetTypeController {

    private final AssetTypeService assetTypeService;
    private final CategoryService categoryService;

    //list
    @GetMapping
    public String list(@RequestParam(name = "keyword", required = false) String keyword,
                       @RequestParam(name = "categoryId", required = false) Integer categoryId,
                       @RequestParam(name = "typeClass", required = false) AssetTypeClass typeClass,
                       @RequestParam(name = "depreciationMethod", required = false) DepreciationMethod depreciationMethod,
                       @RequestParam(name = "direction", required = false) String direction,
                       @RequestParam(name = "page", defaultValue = "1") int page,
                       Model model) {

        int size = 3;
        int offset = (page - 1) * size;

        List<AssetTypeResponse> list =
                assetTypeService.search(keyword, categoryId, typeClass, depreciationMethod, direction, offset, size);

        int total =
                assetTypeService.count(keyword, categoryId, typeClass, depreciationMethod);

        int totalPages = (int) Math.ceil((double) total / size);

        model.addAttribute("assetTypes", list);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("typeClass", typeClass);
        model.addAttribute("depreciationMethod", depreciationMethod);
        model.addAttribute("direction", direction);

        loadCommonData(model);

        return "manager/asset-type-list";
    }

   //create
    @GetMapping("/create")
    public String createForm(Model model) {

        model.addAttribute("assetType", new AssetTypeCreateRequest());
        model.addAttribute("mode", "create");

        loadCommonData(model);

        return "manager/asset-type-form";
    }

   //form
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("assetType") AssetTypeCreateRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("mode", "create");
            loadCommonData(model);

            return "manager/asset-type-form";
        }

        try {

            assetTypeService.create(request);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo loại tài sản thành công!");

            return "redirect:/manager/asset-types";

        } catch (InvalidDataException e) {

            model.addAttribute("mode", "create");
            model.addAttribute("errorMessage", e.getMessage());
            loadCommonData(model);

            return "manager/asset-type-form";
        }
    }

  //edit
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

        return "manager/asset-type-form";
    }

  //update
    @PostMapping("/update")
    public String update(@Valid @ModelAttribute("assetType") AssetTypeUpdateRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("mode", "update");
            loadCommonData(model);

            return "manager/asset-type-form";
        }

        try {

            assetTypeService.update(request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thành công!");

            return "redirect:/manager/asset-types";

        } catch (InvalidDataException ex) {

            model.addAttribute("mode", "update");
            model.addAttribute("errorMessage", ex.getMessage());

            loadCommonData(model);

            return "manager/asset-type-form";
        }
    }

  //delete
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id,
                         RedirectAttributes redirectAttributes) {

        try {

            assetTypeService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa thành công!");

        } catch (InvalidDataException ex) {

            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/manager/asset-types";
    }

   //load data
    private void loadCommonData(Model model) {

        model.addAttribute("typeClasses", AssetTypeClass.values());
        model.addAttribute("depreciationMethods", DepreciationMethod.values());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("activeMenu", "asset-type");
    }
}