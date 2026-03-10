package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.request.TicketDetailRequestDto;
import edu.fpt.groupfive.dto.warehouse.request.TicketFormDto;
import edu.fpt.groupfive.model.warehouse.InventoryTicket;
import edu.fpt.groupfive.model.warehouse.TicketDetail;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.warehouse.InventoryTicketService;
import edu.fpt.groupfive.service.warehouse.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/wh/{userId}/tickets")
@RequiredArgsConstructor
public class InventoryTicketController {

    private final InventoryTicketService ticketService;
    private final WarehouseService warehouseService;
    private final AssetTypeService assetTypeService;

    @GetMapping
    public String list(@PathVariable("userId") Integer userId, Model model, RedirectAttributes redirectAttributes) {
        try {
            Integer warehouseId = warehouseService.getWarehouseByManager(userId).getId();
            model.addAttribute("tickets", ticketService.getTicketsByWarehouseId(warehouseId));
            model.addAttribute("userId", userId);
            model.addAttribute("activeMenu", "ticket");
            return "warehouse/ticket-list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/wh/" + userId;
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("userId") Integer userId,
            @PathVariable("id") Integer id,
            Model model) {
        InventoryTicket ticket = ticketService.getTicketById(id);
        if (ticket == null) {
            return "redirect:/wh/" + userId + "/tickets";
        }

        List<TicketDetail> details = ticketService.getDetailsByTicketId(id);

        model.addAttribute("ticket", ticket);
        model.addAttribute("details", details);
        model.addAttribute("userId", userId);
        model.addAttribute("activeMenu", "ticket");
        model.addAttribute("assetTypeMap", assetTypeService.getAssetTypeIdToNameMap());

        return "warehouse/ticket-detail";
    }

    @GetMapping("/add")
    public String addForm(@PathVariable("userId") Integer userId, Model model) {
        TicketFormDto form = new TicketFormDto();
        form.getDetails().add(new TicketDetailRequestDto()); // Item 1
        form.getDetails().add(new TicketDetailRequestDto()); // Item 2
        form.getDetails().add(new TicketDetailRequestDto()); // Item 3

        model.addAttribute("form", form);
        model.addAttribute("assetTypes", assetTypeService.getAll());
        model.addAttribute("userId", userId);
        model.addAttribute("mode", "create");
        model.addAttribute("activeMenu", "ticket");
        return "warehouse/ticket-form";
    }

    @PostMapping("/add")
    public String add(@PathVariable("userId") Integer userId,
            @ModelAttribute("form") TicketFormDto form,
            RedirectAttributes redirectAttributes) {
        try {
            Integer warehouseId = warehouseService.getWarehouseByManager(userId).getId();

            InventoryTicket ticket = new InventoryTicket();
            ticket.setWarehouseId(warehouseId);
            ticket.setTicketType(form.getTicketType());
            ticket.setHandleBy(userId);

            List<TicketDetail> entities = form.getDetails().stream()
                    .filter(d -> d.getAssetTypeId() != null && d.getQuantity() != null && d.getQuantity() > 0)
                    .map(d -> {
                        TicketDetail td = new TicketDetail();
                        td.setAssetTypeId(d.getAssetTypeId());
                        td.setQuantity(d.getQuantity());
                        td.setNote(d.getNote());
                        return td;
                    })
                    .collect(Collectors.toList());

            if (entities.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Phải có ít nhất 1 loại tài sản trong phiếu.");
                return "redirect:/wh/" + userId + "/tickets/add";
            }

            ticketService.createTicket(ticket, entities);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo phiếu thành công!");
            return "redirect:/wh/" + userId + "/tickets";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/wh/" + userId + "/tickets/add";
        }
    }

}
