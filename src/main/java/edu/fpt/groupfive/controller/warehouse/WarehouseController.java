package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.WarehouseCreateRequest;
import edu.fpt.groupfive.dto.warehouse.WarehouseUpdateRequest;
import edu.fpt.groupfive.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/warehouse/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping("/list")
    public String getAll(Model model) {
        model.addAttribute("warehouses", warehouseService.getAllWarehouses());
        return "warehouse/warehouse-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        // model.addAttribute("warehouse", new WarehouseCreateRequest());
        return "warehouse/warehouse-form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute WarehouseCreateRequest request) {
        warehouseService.createWarehouse(request);
        return "redirect:/warehouse/warehouses/list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("warehouse", warehouseService.getWarehouseById(id));
        return "warehouse/warehouse-form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable("id") Integer id, @ModelAttribute WarehouseUpdateRequest request) {
        request.setId(id);
        warehouseService.updateWarehouse(request);
        return "redirect:/warehouse/warehouses/list";
    }
}
