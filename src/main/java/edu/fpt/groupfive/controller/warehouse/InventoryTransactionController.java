package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.TransactionCreateRequest;
import edu.fpt.groupfive.service.warehouse.InventoryTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/warehouse/transactions")
public class InventoryTransactionController {

    private final InventoryTransactionService transactionService;

    @GetMapping("/ticket/{ticketId}")
    public String getByTicketId(@PathVariable("ticketId") Integer ticketId, Model model) {
        model.addAttribute("transactions", transactionService.getTransactionsByTicketId(ticketId));
        return "warehouse/transaction-list";
    }

    @GetMapping("/asset/{assetId}")
    public String getByAssetId(@PathVariable("assetId") Integer assetId, Model model) {
        model.addAttribute("transactions", transactionService.getTransactionsByAssetId(assetId));
        return "warehouse/transaction-list";
    }

    @PostMapping("/create")
    public String logTransaction(@ModelAttribute TransactionCreateRequest request) {
        transactionService.logTransaction(request);
        return "redirect:/warehouse/transactions/ticket/" + request.getTicketId();
    }
}
