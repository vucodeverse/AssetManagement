package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.WarehouseReqDto;
import edu.fpt.groupfive.dto.warehouse.WarehouseRespDto;
import edu.fpt.groupfive.mapper.warehouse.WarehouseMapper;
import edu.fpt.groupfive.model.warehouse.Warehouse;
import edu.fpt.groupfive.service.UserService;
import edu.fpt.groupfive.service.warehouse.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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

        return "redirect:/wh/warehouses/" + newWarehouse.getId()+"/detail";

    }


    @GetMapping(path = "/{id}/detail")
    public String showWarehouseDetails(@PathVariable("id") Integer id, Model model) {
        WarehouseRespDto warehouseDetail = warehouseService.getWarehouseDetail(id);
        model.addAttribute("warehouse", warehouseDetail);
        return "page/warehouse/detail-view";
    }

    @GetMapping
    public String showWarehouseList(Model model) {
        model.addAttribute("warehouses", warehouseService.getAllWarehouse());
        return "page/warehouse/list-view";
    }


    @PostMapping(path = "/{id}/active")
    public String activeWarehouse(@PathVariable("id") Integer id) {
        warehouseService.activeWarehouse(id);
        return "redirect:/wh/warehouses/" + id+"/detail";
    }

    @GetMapping(path = "/{id}/edit")
    public String editWarehouse(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("warehouseId", id);

        Warehouse wh = warehouseService.getWarehouse(id);

        model.addAttribute("warehouse", new WarehouseReqDto(wh.getName(), wh.getAddress(), wh.getManagerId()));

        loadFormData(model);
        return "page/warehouse/edit-form";
    }

    @PostMapping(path = "/{id}/edit")
    public String editWarehouse(
            @PathVariable("id") Integer id,
            @ModelAttribute
            @Valid
            WarehouseReqDto request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            loadFormData(model);
            return "page/warehouse/edit-form";
        }

        warehouseService.updateWarehouse(id, request);
        return String.format("redirect:/wh/warehouses/%d/detail", id);

    }

}
