package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.TicketAssetMappingRequest;
import edu.fpt.groupfive.service.warehouse.TicketAssetMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/warehouse/ticket-asset-mappings")
public class TicketAssetMappingController {

    @Autowired
    private TicketAssetMappingService mappingService;

    @GetMapping("/detail/{detailId}")
    public String getMappingsByDetailId(@PathVariable Integer detailId, Model model) {
        model.addAttribute("mappings", mappingService.getMappingsByDetailId(detailId));
        return "warehouse/ticket-asset-mapping-list";
    }

    @PostMapping("/create")
    public String mapAssetToDetail(@ModelAttribute TicketAssetMappingRequest request) {
        mappingService.mapAssetToTicketDetail(request);
        return "redirect:/warehouse/ticket-asset-mappings/detail/" + request.getDetailId();
    }
}
