package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.service.warehouse.WarehouseOutboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/warehouse/outbound")
@RequiredArgsConstructor
public class WarehouseOutboundController {

    private final WarehouseOutboundService outboundService;

    @PostMapping("/allocation")
    public String processAllocation(@RequestParam("allocationRequestId") Integer allocationRequestId,
                                    @RequestParam("assetId") Integer assetId) {
        outboundService.processAllocationOutbound(allocationRequestId, assetId);
        return "redirect:/warehouse/dashboard";
    }
}
