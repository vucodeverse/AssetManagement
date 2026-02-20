package edu.fpt.groupfive.controller.manager;

import edu.fpt.groupfive.dto.request.CategoryCreateRequest;
import edu.fpt.groupfive.dto.request.CategoryUpdateRequest;
import edu.fpt.groupfive.dto.response.CategoryResponse;
import edu.fpt.groupfive.service.CategoryService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequiredArgsConstructor
@RequestMapping("/manager/categories")
public class CategoryController {

    private final CategoryService categoryService;

//load page

    @GetMapping
    public String viewPage(

            @RequestParam( value = "id", required = false) Integer id, Model model
    ) {
  //load view form ben trai
        List<CategoryResponse> categories = categoryService.getAll();
        model.addAttribute("categories", categories);
        String mode = "create";



        //form create ben trai
        if(id !=null){

            CategoryResponse response = categoryService.getById(id);

            CategoryUpdateRequest dto = new CategoryUpdateRequest();
            dto.setCategoryId(response.getCategoryId());
            dto.setCategoryName(response.getCategoryName());
            dto.setDescription(response.getDescription());
            dto.setStatus(response.getStatus());

            model.addAttribute("category", dto);
            mode="update";
        }else{
            model.addAttribute("category", new CategoryCreateRequest());
        }
        model.addAttribute("mode", mode);
        return "manager/category-page";
    }


    //create
    @PostMapping(params = "add")
    public  String create(@ModelAttribute("category") CategoryCreateRequest request, Model model){
        try {
            categoryService.create(request);
        }catch (InvalidDataException e){

            model.addAttribute("categories", categoryService.getAll());
            model.addAttribute("mode", "create");
            model.addAttribute("errorMessage", e.getMessage());

            return "manager/category-page";
        }

        return "redirect:/manager/categories";
    }

    //update
    @PostMapping(params = "save")
    public String update(@ModelAttribute("category") CategoryUpdateRequest request, Model model){

        try {
            categoryService.update(request);
        } catch (RuntimeException e) {

            model.addAttribute("categories", categoryService.getAll());
            model.addAttribute("mode", "update");
            model.addAttribute("errorMessage", e.getMessage());

            return "manager/category-page";
        }

        return "redirect:/manager/categories";
    }

    //delete
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id){
        categoryService.delete(id);
        return "redirect:/manager/categories";
    }

}

