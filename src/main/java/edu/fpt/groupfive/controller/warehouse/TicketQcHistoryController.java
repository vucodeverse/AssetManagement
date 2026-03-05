package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.QcHistoryCreateRequest;
import edu.fpt.groupfive.service.warehouse.TicketQcHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/warehouse/qc-history")
public class TicketQcHistoryController {

    private final TicketQcHistoryService qcHistoryService;

    @GetMapping("/ticket/{ticketId}")
    public String getByTicketId(@PathVariable Integer ticketId, Model model) {
        model.addAttribute("qcHistories", qcHistoryService.getQcHistoryByTicketId(ticketId));
        return "warehouse/qc-history-list";
    }

    @PostMapping("/create")
    public String createQcHistory(@ModelAttribute QcHistoryCreateRequest request) {
        qcHistoryService.createQcHistory(request);
        return "redirect:/warehouse/qc-history/ticket/" + request.getTicketId();
    }
}
