package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.AuditScanRequest;
import edu.fpt.groupfive.service.warehouse.AuditScanRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/warehouse/audit-scans")
public class AuditScanRecordController {

    private final AuditScanRecordService scanRecordService;

    @GetMapping("/audit/{auditId}")
    public String getRecordsByAuditId(@PathVariable Integer auditId, Model model) {
        model.addAttribute("scans", scanRecordService.getRecordsByAuditId(auditId));
        model.addAttribute("auditId", auditId);
        return "warehouse/audit-scan-list";
    }

    @PostMapping("/create")
    public String scanAsset(@ModelAttribute AuditScanRequest request) {
        scanRecordService.scanAsset(request);
        return "redirect:/warehouse/audit-scans/audit/" + request.getAuditId();
    }
}
