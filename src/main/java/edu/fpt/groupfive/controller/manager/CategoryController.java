package edu.fpt.groupfive.controller.manager;

import edu.fpt.groupfive.dto.request.CategoryCreateRequest;
import edu.fpt.groupfive.dto.request.CategoryUpdateRequest;
import edu.fpt.groupfive.dto.response.CategoryResponse;
import edu.fpt.groupfive.dto.response.PageResponse;
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
@RequiredArgsConstructor
@RequestMapping("/manager/categories")
public class CategoryController {

    private final CategoryService categoryService;

//load page

    @GetMapping
    public String viewPage(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sort", required = false) String sort,
            Model model
    ) {

        String direction = null;
        if ("asc".equalsIgnoreCase(sort)) {
            direction = "ASC";
        } else if ("desc".equalsIgnoreCase(sort)) {
            direction = "DESC";
        }
        //load view form ben trai

        loadCategoryPage(model, keyword, direction, page);
        model.addAttribute("keyword", keyword);
        String mode = "create";


        //form create ben trai
        if (id != null) {

            CategoryResponse response = categoryService.getById(id);

            CategoryUpdateRequest dto = new CategoryUpdateRequest();
            dto.setCategoryId(response.getCategoryId());
            dto.setCategoryName(response.getCategoryName());
            dto.setDescription(response.getDescription());
            dto.setStatus(response.getStatus());

            model.addAttribute("category", dto);
            mode = "update";
        } else {
            model.addAttribute("category", new CategoryCreateRequest());
        }
        model.addAttribute("mode", mode);
        model.addAttribute("activeMenu", "category");
        return "manager/category-page";
    }


    //create
    @PostMapping(params = "add")
    public String create(@Valid @ModelAttribute("category") CategoryCreateRequest request, BindingResult result, Model model, RedirectAttributes redirectAttributes) {

        if (!result.hasErrors()) {
            try {
                categoryService.create(request);
                redirectAttributes.addFlashAttribute("success", "Thêm danh mục thành công");
                return "redirect:/manager/categories";

            } catch (InvalidDataException e) {

                // thêm lỗi vào BindingResult
                result.rejectValue("categoryName", null, e.getMessage());
            }
        }

        if (result.hasErrors()) {
            loadCategoryPage(model, null, null, 1);

            model.addAttribute("mode", "create");
            model.addAttribute("activeMenu", "category");

            return "manager/category-page";
        }


        return "redirect:/manager/categories";
    }

    //update
    @PostMapping(params = "save")
    public String update(@Valid @ModelAttribute("category") CategoryUpdateRequest request, BindingResult result, Model model, RedirectAttributes redirectAttributes) {

        if (!result.hasErrors()) {
            try {
                categoryService.update(request);
                redirectAttributes.addFlashAttribute("success", "Cập nhật danh mục thành công.");
                return "redirect:/manager/categories";

            } catch (InvalidDataException e) {
                result.rejectValue("categoryName", null, e.getMessage());
            }
        }

        if (result.hasErrors()) {
            loadCategoryPage(model, null, null, 1);
            model.addAttribute("mode", "update");
            model.addAttribute("activeMenu", "category");
            return "manager/category-page";
        }
        return "redirect:/manager/categories";
    }

    //delete
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {

        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa danh mục thành công.");

        } catch (InvalidDataException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());

        }
        return "redirect:/manager/categories";

    }


    private void loadCategoryPage(Model model,
                                  String keyword,
                                  String direction,
                                  int page) {

        PageResponse<CategoryResponse> result =
                categoryService.searchAndSort(keyword, direction, page);

        model.addAttribute("categories", result.getData());
        model.addAttribute("page", result);

    }

}

