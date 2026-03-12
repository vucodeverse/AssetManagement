package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.request.TicketDetailRequestDto;
import edu.fpt.groupfive.dto.warehouse.request.TicketFormDto;
import edu.fpt.groupfive.dto.warehouse.response.InventoryTicketResponseDto;
import edu.fpt.groupfive.dto.warehouse.response.TicketDetailResponseDto;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.warehouse.InventoryTicketService;
import edu.fpt.groupfive.service.warehouse.impl.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import edu.fpt.groupfive.service.warehouse.TicketMappingService;

@Controller
@RequestMapping("/wh/{userId}/tickets")
@RequiredArgsConstructor
public class InventoryTicketController {

    private final InventoryTicketService ticketService;
    private final WarehouseService warehouseService;
    private final AssetTypeService assetTypeService;
    private final TicketMappingService ticketMappingService;

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
        InventoryTicketResponseDto ticket = ticketService.getTicketById(id);
        if (ticket == null) {
            return "redirect:/wh/" + userId + "/tickets";
        }

        List<TicketDetailResponseDto> details = ticketService.getDetailsByTicketId(id);

        try {
            model.addAttribute("mappingData", ticketMappingService.getMappingDetails(id));
        } catch (Exception e) {
            // Ignore if mapping data cannot be fetched
        }

        model.addAttribute("ticket", ticket);
        model.addAttribute("details", details);
        model.addAttribute("userId", userId);
        model.addAttribute("activeMenu", "ticket");

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

            boolean hasValidDetail = form.getDetails().stream()
                    .anyMatch(d -> d.getAssetTypeId() != null && d.getQuantity() != null && d.getQuantity() > 0);

            if (!hasValidDetail) {
                redirectAttributes.addFlashAttribute("errorMessage", "Phải có ít nhất 1 loại tài sản trong phiếu.");
                return "redirect:/wh/" + userId + "/tickets/add";
            }

            ticketService.createTicket(warehouseId, userId, form);

            redirectAttributes.addFlashAttribute("successMessage", "Tạo phiếu thành công!");
            return "redirect:/wh/" + userId + "/tickets";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/wh/" + userId + "/tickets/add";
        }
    }

}
