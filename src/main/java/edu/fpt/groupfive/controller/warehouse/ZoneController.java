package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.ZoneCreateRequest;
import edu.fpt.groupfive.dto.warehouse.ZoneUpdateRequest;
import edu.fpt.groupfive.service.warehouse.ZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/warehouse/zones")
public class ZoneController {

    @Autowired
    private ZoneService zoneService;

    @GetMapping("/warehouse/{warehouseId}")
    public String getByWarehouseId(@PathVariable Integer warehouseId, Model model) {
        model.addAttribute("zones", zoneService.getZonesByWarehouseId(warehouseId));
        model.addAttribute("warehouseId", warehouseId);
        return "warehouse/zone-list";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute ZoneCreateRequest request) {
        zoneService.createZone(request);
        return "redirect:/warehouse/zones/warehouse/" + request.getWarehouseId();
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute ZoneUpdateRequest request) {
        request.setId(id);
        zoneService.updateZone(request);
        return "redirect:/warehouse/zones/warehouse/" + request.getWarehouseId();
    }
}
