package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.QCReportRequestDTO;
import edu.fpt.groupfive.service.warehouse.WarehouseOutboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        model.addAttribute("allocations", warehouseOutboundService.getAllocations());
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

    @PostMapping("/{handover_id}/scan")
    public String processAllocationScan(
            @PathVariable("handover_id") Integer handoverId,
            @RequestParam("assetCode") String assetCode,
            RedirectAttributes ra) {
        try {
            boolean isCompleted = warehouseOutboundService.processScan(handoverId, assetCode, 1); // Mock executedBy = 1
            if (isCompleted) {
                ra.addFlashAttribute(SUCCESS_MSG, "Đã quét đủ số lượng tài sản. Lệnh xuất kho #" + handoverId + " đã hoàn tất.");
                return REDIRECT_OUTBOUND_LIST;
            }
            ra.addFlashAttribute(SUCCESS_MSG, "Đã quét và gán tài sản " + assetCode + " thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute(ERROR_MSG, "Lỗi khi quét tài sản: " + e.getMessage());
        }
        return REDIRECT_OUTBOUND_DETAIL + handoverId;
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

    @PostMapping("/{handover_id}/complete")
    public String completeOutbound(
            @PathVariable("handover_id") Integer handoverId,
            RedirectAttributes ra) {
        
        ra.addFlashAttribute(SUCCESS_MSG, "Lệnh xuất kho #" + handoverId + " đã được hoàn tất thành công (giả định).");
        return REDIRECT_OUTBOUND_LIST;
    }
}
