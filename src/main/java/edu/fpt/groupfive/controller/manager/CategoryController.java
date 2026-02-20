package edu.fpt.groupfive.controller.manager;

import edu.fpt.groupfive.dto.request.CategoryCreateRequest;
import edu.fpt.groupfive.dto.request.CategoryUpdateRequest;
import edu.fpt.groupfive.dto.response.CategoryResponse;
import edu.fpt.groupfive.service.CategoryService;
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

            @RequestParam( value = "id", required = false) Integer id,
            @RequestParam(value = "mode", required = false, defaultValue = "create") String mode,
            Model model
    ) {
  //load view form ben trai
        List<CategoryResponse> categories = categoryService.getAll();
        model.addAttribute("categories", categories);



        //form create ben trai
        if(id !=null){
            mode="update";
            CategoryResponse response = categoryService.getById(id);

            CategoryUpdateRequest dto = new CategoryUpdateRequest();
            dto.setCategoryId(response.getCategoryId());
            dto.setCategoryName(response.getCategoryName());
            dto.setDescription(response.getDescription());
            dto.setStatus(response.getStatus());

            model.addAttribute("category", dto);

        }else{
            model.addAttribute("category", new CategoryCreateRequest());
            mode="create";
        }
        model.addAttribute("mode", mode);
        return "manager/category-page";
    }


    //create
    @PostMapping(params = "add")
    public  String create(@ModelAttribute("category") CategoryCreateRequest request){
        categoryService.create(request);
        return "redirect:/manager/categories";
    }

    //update
    @PostMapping(params = "save")
    public String update(@ModelAttribute("category") CategoryUpdateRequest request){
        categoryService.update(request);
        return "redirect:/manager/categories";
    }

    //delete
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id){
        categoryService.delete(id);
        return "redirect:/manager/categories";
    }

}

