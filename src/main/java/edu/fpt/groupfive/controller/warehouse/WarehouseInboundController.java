package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.request.InboundPORequest;
import edu.fpt.groupfive.dto.warehouse.request.InboundReturnRequest;
import edu.fpt.groupfive.service.warehouse.WarehouseInboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/warehouse/inbound")
@RequiredArgsConstructor
public class WarehouseInboundController {

    private final WarehouseInboundService inboundService;

    @PostMapping("/po")
    public String processPO(@ModelAttribute InboundPORequest request) {
        inboundService.processPOInbound(request);
        return "redirect:/warehouse/dashboard";
    }

    @PostMapping("/return")
    public String processReturn(@ModelAttribute InboundReturnRequest request) {
        inboundService.processReturnInbound(request);
        return "redirect:/warehouse/dashboard";
    }
}
