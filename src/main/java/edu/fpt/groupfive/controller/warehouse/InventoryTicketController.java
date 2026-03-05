package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.TicketCreateRequest;
import edu.fpt.groupfive.service.warehouse.InventoryTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/warehouse/tickets")
public class InventoryTicketController {

    @Autowired
    private InventoryTicketService ticketService;

    @GetMapping("/warehouse/{warehouseId}")
    public String getTicketsByWarehouseId(@PathVariable Integer warehouseId, Model model) {
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
    public String getTicketById(@PathVariable Integer id, Model model) {
        model.addAttribute("ticket", ticketService.getTicketById(id));
        return "warehouse/ticket-detail";
    }
}
