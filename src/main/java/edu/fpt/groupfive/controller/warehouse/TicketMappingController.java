package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.response.TicketMappingResponseDto;
import edu.fpt.groupfive.service.warehouse.TicketMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/wh/{userId}/tickets/{ticketId}/mapping")
@RequiredArgsConstructor
public class TicketMappingController {

    private final TicketMappingService ticketMappingService;

    @GetMapping
    public String viewMappingForm(@PathVariable("userId") Integer userId,
            @PathVariable("ticketId") Integer ticketId,
            Model model) {
        try {
            TicketMappingResponseDto mappingConfig = ticketMappingService.getMappingDetails(ticketId);
            model.addAttribute("mappingData", mappingConfig);
            model.addAttribute("userId", userId);
            model.addAttribute("ticketId", ticketId);
            model.addAttribute("activeMenu", "ticket");
            return "warehouse/ticket-mapping";
        } catch (Exception e) {
            return "redirect:/wh/" + userId + "/tickets/" + ticketId;
        }
    }

    @PostMapping("/scan")
    public String scanAsset(@PathVariable("userId") Integer userId,
            @PathVariable("ticketId") Integer ticketId,
            @RequestParam("scannedAssetId") Integer scannedAssetId,
            RedirectAttributes redirectAttributes) {
        try {
            ticketMappingService.mapScannedAsset(ticketId, scannedAssetId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã nhận tài sản ID: " + scannedAssetId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/wh/" + userId + "/tickets/" + ticketId + "/mapping";
    }

    @PostMapping("/remove")
    public String removeScannedAsset(@PathVariable("userId") Integer userId,
            @PathVariable("ticketId") Integer ticketId,
            @RequestParam("detailId") Integer detailId,
            @RequestParam("assetId") Integer assetId,
            RedirectAttributes redirectAttributes) {
        try {
            ticketMappingService.removeScannedAsset(ticketId, detailId, assetId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tài sản khỏi phiếu.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/wh/" + userId + "/tickets/" + ticketId + "/mapping";
    }

    @PostMapping("/submit")
    public String submitTicket(@PathVariable("userId") Integer userId,
            @PathVariable("ticketId") Integer ticketId,
            RedirectAttributes redirectAttributes) {
        try {
            ticketMappingService.validateAndSubmitTicket(ticketId);
            redirectAttributes.addFlashAttribute("successMessage", "Chốt phiếu và cập nhật kho thành công!");
            return "redirect:/wh/" + userId + "/tickets";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/wh/" + userId + "/tickets/" + ticketId + "/mapping";
        }
    }
}
