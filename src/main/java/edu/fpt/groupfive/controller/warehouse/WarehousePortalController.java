package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.request.warehouse.TransactionFilterRequestDTO;
import edu.fpt.groupfive.dto.response.warehouse.AssetLocationResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.DashboardResponseDTO;
import edu.fpt.groupfive.service.warehouse.WhTransactionService;
import edu.fpt.groupfive.service.warehouse.WhZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/wh")
@RequiredArgsConstructor
public class WarehousePortalController {

    private final WhZoneService whZoneService;
    private final WhTransactionService whTransactionService;

    // =========================================================
    // DASHBOARD — GET /wh/dashboard
    // =========================================================
    @GetMapping("/dashboard")
    public String dashboard(
            Model model,
            Principal principal) {
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("pageTitle", "Tổng quan Kho - Warehouse");

        DashboardResponseDTO dashboard = DashboardResponseDTO.builder()
                .pendingInboundPO(3)
                .pendingInboundReturn(5)
                .pendingOutboundAllocation(2)
                .capacityHeatmap(whZoneService.getAllZones())
                .recentActivities(whTransactionService.getAllTransactions(new TransactionFilterRequestDTO()).stream().limit(5).collect(Collectors.toList()))
                .build();

        model.addAttribute("dashboard", dashboard);
        return "warehouse/dashboard";
    }

    // =========================================================
    // LOCATOR — GET /wh/locator
    // =========================================================
    @GetMapping("/locator")
    public String locator(@RequestParam(value = "assetCode", required = false) String assetCode, Model model) {
        model.addAttribute("activeMenu", "locator");
        model.addAttribute("pageTitle", "Tra cứu Tài sản - Warehouse");

        if (assetCode != null && !assetCode.isBlank()) {
            try {
                AssetLocationResponseDTO dto = whZoneService.findAssetLocation(assetCode);
                model.addAttribute("asset", dto);
            } catch (IllegalArgumentException e) {
                model.addAttribute("errorMessage", e.getMessage());
            }
        }
        return "warehouse/locator";
    }

    // =========================================================
    // LEDGER — GET /wh/ledger
    // =========================================================
    @GetMapping("/ledger")
    public String ledger(TransactionFilterRequestDTO filter, Model model) {
        model.addAttribute("activeMenu", "ledger");
        model.addAttribute("pageTitle", "Sổ cái Giao dịch - Warehouse");
        model.addAttribute("transactions", whTransactionService.getAllTransactions(filter));
        model.addAttribute("filter", filter);
        return "warehouse/ledger_list";
    }
}
