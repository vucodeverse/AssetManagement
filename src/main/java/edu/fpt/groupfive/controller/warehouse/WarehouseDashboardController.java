package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.TicketResponse;
import edu.fpt.groupfive.dto.warehouse.WarehouseResponse;
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

        @GetMapping("/{warehouseId}")
        public String viewDashboard(@PathVariable("warehouseId") Integer warehouseId, Model model) {

                return "warehouse/dashboard";
        }
}
