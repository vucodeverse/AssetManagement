package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.AuditCreateRequest;
import edu.fpt.groupfive.dto.warehouse.AuditUpdateRequest;
import edu.fpt.groupfive.service.warehouse.InventoryAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/warehouse/audits")
public class InventoryAuditController {

    @Autowired
    private InventoryAuditService auditService;

    @GetMapping("/warehouse/{warehouseId}")
    public String getAuditsByWarehouseId(@PathVariable Integer warehouseId, Model model) {
        model.addAttribute("audits", auditService.getAuditsByWarehouseId(warehouseId));
        model.addAttribute("warehouseId", warehouseId);
        return "warehouse/audit-list";
    }

    @PostMapping("/create")
    public String createAudit(@ModelAttribute AuditCreateRequest request) {
        auditService.createAudit(request);
        return "redirect:/warehouse/audits/warehouse/" + request.getWarehouseId();
    }

    @GetMapping("/{id}")
    public String getAuditById(@PathVariable Integer id, Model model) {
        model.addAttribute("audit", auditService.getAuditById(id));
        return "warehouse/audit-detail";
    }

    @PostMapping("/edit/{id}")
    public String updateAudit(@PathVariable Integer id, @ModelAttribute AuditUpdateRequest request) {
        request.setId(id);
        // Assuming we need to redirect back to warehouse list, but we don't have
        // warehouseId easily here.
        // In a real scenario, we might pass warehouseId in the update request or fetch
        // it.
        auditService.updateAudit(request);
        return "redirect:/warehouse/audits/" + id;
    }
}
