package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.WarehouseReqDto;
import edu.fpt.groupfive.dto.warehouse.WarehouseRespDto;
import edu.fpt.groupfive.mapper.warehouse.WarehouseMapper;
import edu.fpt.groupfive.model.Users;
import edu.fpt.groupfive.model.warehouse.Warehouse;
import edu.fpt.groupfive.service.UserService;
import edu.fpt.groupfive.service.warehouse.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/wh/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final UserService userService;
    private final WarehouseMapper warehouseMapper;

    private void loadFormData(Model model) {

        Map<Integer, String> warehouseStaffs = userService.getAllWarehouseStaffName();
        model.addAttribute("warehouseStaffs", warehouseStaffs);
    }

    @GetMapping(path = "/add")
    public String showAddWarehouseForm(Model model) {
        loadFormData(model);
        model.addAttribute("warehouse", WarehouseReqDto.builder().build());
        return "page/warehouse/add-form";
    }

    @PostMapping(path = "/add")
    public String addWarehouse(
            @Valid
            @ModelAttribute("warehouse")
            WarehouseReqDto request,
            BindingResult result,
            Model model
    ) {

        if (result.hasErrors()) {
            loadFormData(model);
            return "page/warehouse/add-form";
        }

        Warehouse newWarehouse = warehouseService.createWarehouse(request);

        return "redirect:/wh/warehouses/" + newWarehouse.getId();

    }


    @GetMapping(path = "/{id}")
    public String showWarehouseDetails(@PathVariable("id") Integer id, Model model) {
        WarehouseRespDto warehouseDetail = warehouseService.getWarehouseDetail(id);
        model.addAttribute("warehouse", warehouseDetail);
        return "page/warehouse/detail-view";
    }
}
