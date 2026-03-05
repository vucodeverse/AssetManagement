package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.WarehouseCreateRequest;
import edu.fpt.groupfive.dto.warehouse.WarehouseUpdateRequest;
import edu.fpt.groupfive.service.warehouse.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/warehouse/warehouses")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

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
    public String showEditForm(@PathVariable Integer id, Model model) {
        model.addAttribute("warehouse", warehouseService.getWarehouseById(id));
        return "warehouse/warehouse-form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute WarehouseUpdateRequest request) {
        request.setId(id);
        warehouseService.updateWarehouse(request);
        return "redirect:/warehouse/warehouses/list";
    }
}
