package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.QCReportRequestDTO;
import edu.fpt.groupfive.service.warehouse.WarehouseOutboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/wh/outbound")
@RequiredArgsConstructor
public class WarehouseOutboundController {

    private static final String SUCCESS_MSG = "successMessage";
    private static final String ERROR_MSG = "errorMessage";
    private static final String ACTIVE_MENU = "activeMenu";
    private static final String OUTBOUND = "outbound";
    private static final String PAGE_TITLE = "pageTitle";
    private static final String REDIRECT_OUTBOUND_LIST = "redirect:/wh/outbound";
    private static final String REDIRECT_OUTBOUND_DETAIL = "redirect:/wh/outbound/";
    
    private final WarehouseOutboundService warehouseOutboundService;

    @GetMapping("")
    public String allocationListPage(Model model) {
        model.addAttribute(ACTIVE_MENU, OUTBOUND);
        model.addAttribute(PAGE_TITLE, "Xuất kho Cấp phát - Warehouse");
        model.addAttribute("allocations", warehouseOutboundService.getPendingAllocations());
        return "warehouse/outbound/allocation_list";
    }

    @GetMapping("/{handover_id}")
    public String allocationDetailPage(@PathVariable("handover_id") Integer handoverId, Model model) {
        HandoverDetailResponseDTO detail = warehouseOutboundService.getHandoverDetail(handoverId);
        
        if (detail == null) {
            return REDIRECT_OUTBOUND_LIST;
        }

        model.addAttribute(ACTIVE_MENU, OUTBOUND);
        model.addAttribute(PAGE_TITLE, "Chi tiết Cấp phát #" + handoverId);
        model.addAttribute("handover", detail);
            
        return "warehouse/outbound/allocation_detail";
    }

    @PostMapping("/{handover_id}/confirm")
    public String confirmOutbound(
            @PathVariable("handover_id") Integer handoverId,
            @RequestParam Map<String, String> allParams,
            Principal principal,
            RedirectAttributes ra) {
        
        if (principal == null) return "redirect:/login";
        
        try {
            // Lọc các param có dạng asset_{id} = zoneId
            Map<Integer, Integer> assetMap = new HashMap<>();
            String note = allParams.getOrDefault("note", "");
            
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                if (entry.getKey().startsWith("asset_")) {
                    int assetId = Integer.parseInt(entry.getKey().substring(6));
                    int zoneId = Integer.parseInt(entry.getValue());
                    assetMap.put(assetId, zoneId);
                }
            }
            
            if (assetMap.isEmpty()) {
                throw new RuntimeException("Vui lòng chọn ít nhất một tài sản để xuất kho.");
            }
            
            warehouseOutboundService.confirmOutbound(handoverId, assetMap, principal.getName(), note);
            ra.addFlashAttribute(SUCCESS_MSG, "Đã thực hiện xuất kho và tạo phiếu xuất thành công cho lệnh #" + handoverId);
            return REDIRECT_OUTBOUND_LIST;
            
        } catch (Exception e) {
            ra.addFlashAttribute(ERROR_MSG, "Lỗi khi thực hiện xuất kho: " + e.getMessage());
            return REDIRECT_OUTBOUND_DETAIL + handoverId;
        }
    }

    @GetMapping("/{handover_id}/qc-report")
    public String qcReportPage(
            @PathVariable("handover_id") Integer handoverId,
            @RequestParam("assetCode") String assetCode,
            Model model) {
        
        model.addAttribute(ACTIVE_MENU, OUTBOUND);
        model.addAttribute(PAGE_TITLE, "Báo cáo QC Tài sản " + assetCode);
        model.addAttribute("handoverId", handoverId);
        model.addAttribute("assetCode", assetCode);
        model.addAttribute("qcReport", new QCReportRequestDTO());
        
        return "warehouse/outbound/qc_report";
    }

    @PostMapping("/{handover_id}/qc-report")
    public String submitQCReport(
            @PathVariable("handover_id") Integer handoverId,
            @ModelAttribute QCReportRequestDTO qcReport,
            RedirectAttributes ra) {
        
        ra.addFlashAttribute(SUCCESS_MSG, "Đã ghi nhận báo cáo QC.");
        return REDIRECT_OUTBOUND_DETAIL + handoverId;
    }
}
