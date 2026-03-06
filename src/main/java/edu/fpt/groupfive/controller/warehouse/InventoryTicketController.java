package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.TicketCreateRequest;
import edu.fpt.groupfive.service.warehouse.InventoryTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/warehouse/tickets")
public class InventoryTicketController {

    private final InventoryTicketService ticketService;

    @GetMapping("/warehouse/{warehouseId}")
    public String getTicketsByWarehouseId(@PathVariable("warehouseId") Integer warehouseId, Model model) {
        model.addAttribute("tickets", ticketService.getTicketsByWarehouseId(warehouseId));
        model.addAttribute("warehouseId", warehouseId);
        return "warehouse/ticket-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        return "warehouse/ticket-form";
    }

    @PostMapping("/create")
    public String createTicket(@ModelAttribute TicketCreateRequest request) {
        ticketService.createTicket(request);
        return "redirect:/warehouse/tickets/warehouse/" + request.getWarehouseId();
    }

    @GetMapping("/{id}")
    public String getTicketById(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("ticket", ticketService.getTicketById(id));
        return "warehouse/ticket-detail";
    }
}
