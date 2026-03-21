package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.request.warehouse.TransactionFilterRequestDTO;
import edu.fpt.groupfive.dto.response.warehouse.AssetLocationResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.DashboardResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.LedgerRecordResponseDTO;
import edu.fpt.groupfive.service.warehouse.WhZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/wh")
@RequiredArgsConstructor
public class WarehousePortalController {

    private final WhZoneService whZoneService;

    // =========================================================
    //  DASHBOARD  —  GET /wh/dashboard
    // =========================================================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("pageTitle", "Tổng quan Kho - Warehouse");

        DashboardResponseDTO dashboard = DashboardResponseDTO.builder()
                .pendingInboundPO(3)
                .pendingInboundReturn(5)
                .pendingOutboundAllocation(2)
                .capacityHeatmap(whZoneService.getAllZones())
                .recentActivities(buildDummyLedger().stream().limit(5).collect(Collectors.toList()))
                .build();
        
        model.addAttribute("dashboard", dashboard);
        return "warehouse/dashboard";
    }

    // =========================================================
    //  LOCATOR  —  GET /wh/locator
    // =========================================================
    @GetMapping("/locator")
    public String locator(@RequestParam(value = "assetCode", required = false) String assetCode, Model model) {
        model.addAttribute("activeMenu", "locator");
        model.addAttribute("pageTitle", "Tra cứu Tài sản - Warehouse");

        if (assetCode != null && !assetCode.isBlank()) {
            if ("AST-001".equalsIgnoreCase(assetCode)) {
                model.addAttribute("asset", AssetLocationResponseDTO.builder()
                        .assetCode("AST-001").assetName("Laptop Dell Precision 5550")
                        .status("IN_WAREHOUSE").zoneName("Zone A - Rack 1")
                        .placedBy("Nguyen Van A").placedAt(LocalDateTime.now().minusDays(10))
                        .build());
            } else {
                model.addAttribute("errorMessage", "Không tìm thấy tài sản với mã " + assetCode);
            }
        }
        return "warehouse/locator";
    }

    // =========================================================
    //  LEDGER  —  GET /wh/ledger
    // =========================================================
    @GetMapping("/ledger")
    public String ledger(TransactionFilterRequestDTO filter, Model model) {
        model.addAttribute("activeMenu", "ledger");
        model.addAttribute("pageTitle", "Sổ cái Giao dịch - Warehouse");
        model.addAttribute("transactions", buildDummyLedger());
        return "warehouse/ledger_list";
    }

    private List<LedgerRecordResponseDTO> buildDummyLedger() {
        return List.of(
            LedgerRecordResponseDTO.builder().transactionId(1001).transactionType("INBOUND").assetName("Monitor LG 24\"").zoneName("B-01").executedBy("Admin").executedAt(LocalDateTime.now().minusMinutes(30)).referenceId(101).referenceType("PO").build(),
            LedgerRecordResponseDTO.builder().transactionId(1002).transactionType("OUTBOUND").assetName("Keyboard Corsair").zoneName("A-05").executedBy("Staff 1").executedAt(LocalDateTime.now().minusHours(2)).referenceId(202).referenceType("HANDOVER").build(),
            LedgerRecordResponseDTO.builder().transactionId(1003).transactionType("INBOUND").assetName("Mouse Logitech").zoneName("C-02").executedBy("Admin").executedAt(LocalDateTime.now().minusDays(1)).referenceId(102).referenceType("PO").build()
        );
    }
}
