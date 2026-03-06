package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.AuditResponse;
import edu.fpt.groupfive.dto.warehouse.TicketResponse;
import edu.fpt.groupfive.dto.warehouse.WarehouseResponse;
import edu.fpt.groupfive.service.warehouse.InventoryAuditService;
import edu.fpt.groupfive.service.warehouse.InventoryTicketService;
import edu.fpt.groupfive.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/wh")
public class WarehouseDashboardController {

        private final WarehouseService warehouseService;
        private final InventoryTicketService ticketService;
        private final InventoryAuditService auditService;

        @GetMapping("/{warehouseId}")
        public String viewDashboard(@PathVariable("warehouseId") Integer warehouseId, Model model) {
                WarehouseResponse warehouse = warehouseService.getWarehouseById(warehouseId);

                List<TicketResponse> allTickets = ticketService.getTicketsByWarehouseId(warehouseId);
                List<TicketResponse> pendingInTickets = allTickets.stream()
                                .filter(t -> "IN".equalsIgnoreCase(t.getTicketType()))
                                .filter(t -> "PENDING".equalsIgnoreCase(t.getStatus())
                                                || "IN_PROGRESS".equalsIgnoreCase(t.getStatus()))
                                .collect(Collectors.toList());
                List<TicketResponse> pendingOutTickets = allTickets.stream()
                                .filter(t -> "OUT".equalsIgnoreCase(t.getTicketType()))
                                .filter(t -> "PENDING".equalsIgnoreCase(t.getStatus())
                                                || "IN_PROGRESS".equalsIgnoreCase(t.getStatus()))
                                .collect(Collectors.toList());

                List<AuditResponse> allAudits = auditService.getAuditsByWarehouseId(warehouseId);
                List<AuditResponse> activeAudits = allAudits.stream()
                                .filter(a -> "IN_PROGRESS".equalsIgnoreCase(a.getStatus()))
                                .collect(Collectors.toList());

                model.addAttribute("warehouse", warehouse);
                model.addAttribute("pendingInTickets", pendingInTickets);
                model.addAttribute("pendingOutTickets", pendingOutTickets);
                model.addAttribute("activeAudits", activeAudits);

                return "warehouse/dashboard";
        }
}
