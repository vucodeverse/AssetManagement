package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.request.ZoneRequest;
import edu.fpt.groupfive.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private static final String REDIRECT_ZONES = "redirect:/warehouse/zones";
    private final WarehouseService warehouseService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", warehouseService.getDashboardStats());
        return "warehouse/dashboard";
    }

    @GetMapping("/zones")
    public String zones(Model model) {
        model.addAttribute("zones", warehouseService.getAllZones());
        return "warehouse/zones";
    }

    @PostMapping("/zones/create")
    public String createZone(@ModelAttribute ZoneRequest request) {
        warehouseService.createZone(request);
        return REDIRECT_ZONES;
    }

    @PostMapping("/zones/delete/{id}")
    public String deleteZone(@PathVariable("id") Integer zoneId) {
        warehouseService.deleteZone(zoneId);
        return REDIRECT_ZONES;
    }

    @PostMapping("/zones/reset/{id}")
    public String resetZone(@PathVariable("id") Integer zoneId) {
        warehouseService.resetZone(zoneId);
        return REDIRECT_ZONES;
    }
}
