package edu.fpt.groupfive.controller.manager;

import edu.fpt.groupfive.dto.request.CategoryCreateRequest;
import edu.fpt.groupfive.dto.request.CategoryUpdateRequest;
import edu.fpt.groupfive.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequiredArgsConstructor
@RequestMapping("/manager")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/addCategory")
    public String addForm(Model model) {
        model.addAttribute("category", new CategoryCreateRequest());
        return "manager/add-category";
    }


    @PostMapping("/addCategory")
    public String add(@ModelAttribute("category") CategoryCreateRequest request) {
        categoryService.create(request);
        return "redirect:/manager/list";
    }

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("categories", categoryService.getAll());
        return "manager/category-list";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        categoryService.delete(id);
        return "redirect:/manager/list";
    }

//    @GetMapping("/update/{id}")
//    public String updateForm(@PathVariable Long id, Model model) {
//        model.addAttribute("category", categoryService.getById(id));
//        return "manager/update-category";
//    }


}

