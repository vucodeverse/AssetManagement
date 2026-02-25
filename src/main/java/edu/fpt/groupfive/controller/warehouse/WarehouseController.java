package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.WarehouseReqDto;
import edu.fpt.groupfive.model.warehouse.Warehouse;
import edu.fpt.groupfive.service.warehouse.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/wh/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @ModelAttribute("managers")
    public List<Object> populateManagers() {
        // TODO: call UserService to get List of Manager Response DTO
        return new ArrayList<>();
    }

    @GetMapping(path = "/add")
    public String showAddWarehouseForm(
            Model model
    ){
        model.addAttribute("warehouse", WarehouseReqDto.builder().build());
        return "page/warehouse/add-form";
    }

    @PostMapping(path = "/add")
    public String addWarehouse(
        @Valid
        @ModelAttribute("warehouse")
        WarehouseReqDto warehouseDto,
        BindingResult result,
        RedirectAttributes ra
    ){
        if(result.hasErrors()){
            return "page/warehouse/add-form";
        }

        warehouseService.createWarehouse(warehouseDto);
        ra.addFlashAttribute("success", "Tạo kho thành công!");
        return "redirect:/wh/warehouses";

    }

    @GetMapping
    public String showWarehousesList(
            Model model
    ){
        model.addAttribute("warehouses", warehouseService.getAllWarehouses().stream().map());
    }

}
