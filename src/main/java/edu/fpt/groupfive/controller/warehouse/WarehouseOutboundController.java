package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.response.AssetDetailResponse;
import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.QCReportRequestDTO;
import edu.fpt.groupfive.service.warehouse.WarehouseOutboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

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
        model.addAttribute(PAGE_TITLE, "Tổng quan Cấp phát #" + handoverId);
        model.addAttribute("handover", detail);
            
        return "warehouse/outbound/allocation_detail";
    }

    @GetMapping("/{handover_id}/process")
    public String allocationProcessPage(
            @PathVariable("handover_id") Integer handoverId,
            @RequestParam(value = "codes", required = false) List<String> codes,
            Model model) {
        HandoverDetailResponseDTO detail = warehouseOutboundService.getHandoverDetail(handoverId);
        
        if (detail == null || "COMPLETED".equals(detail.getStatus())) {
            return REDIRECT_OUTBOUND_LIST;
        }

        model.addAttribute(ACTIVE_MENU, OUTBOUND);
        model.addAttribute(PAGE_TITLE, "Xử lý Xuất kho #" + handoverId);
        model.addAttribute("handover", detail);
        
        // Load staged assets
        if (codes != null && !codes.isEmpty()) {
            model.addAttribute("stagedAssets", warehouseOutboundService.getAssetsByCodes(codes));
            model.addAttribute("stagedCodes", codes);
        } else {
            model.addAttribute("stagedAssets", new ArrayList<>());
            model.addAttribute("stagedCodes", new ArrayList<>());
        }
            
        return "warehouse/outbound/allocation_process";
    }

    @PostMapping("/{handover_id}/scan")
    public String processAllocationScan(
            @PathVariable("handover_id") Integer handoverId,
            @RequestParam("assetCode") String assetCode,
            @RequestParam(value = "stagedCodes", required = false) List<String> currentCodes,
            RedirectAttributes ra) {
        
        List<String> codes = currentCodes != null ? new ArrayList<>(currentCodes) : new ArrayList<>();
        
        try {
            // Check if already staged
            if (codes.contains(assetCode)) {
                throw new RuntimeException("Tài sản này đã có trong danh sách chọn.");
            }

            // Validate and add to list
            AssetDetailResponse asset = warehouseOutboundService.validateAssetForOutbound(assetCode, handoverId, codes);
            codes.add(assetCode);
            ra.addFlashAttribute(SUCCESS_MSG, "Đã thêm tài sản '" + asset.getAssetName() + "' (" + assetCode + ") vào danh sách.");
        } catch (Exception e) {
            ra.addFlashAttribute(ERROR_MSG, e.getMessage());
        }

        // Redirect back with all codes
        ra.addAttribute("codes", codes);
        return "redirect:/wh/outbound/" + handoverId + "/process";
    }

    @PostMapping("/{handover_id}/remove-item")
    public String removeStagedItem(
            @PathVariable("handover_id") Integer handoverId,
            @RequestParam("assetCode") String assetCode,
            @RequestParam(value = "stagedCodes", required = false) List<String> currentCodes,
            RedirectAttributes ra) {
        
        List<String> codes = currentCodes != null ? new ArrayList<>(currentCodes) : new ArrayList<>();
        codes.remove(assetCode);
        
        ra.addAttribute("codes", codes);
        return "redirect:/wh/outbound/" + handoverId + "/process";
    }

    @PostMapping("/{handover_id}/confirm")
    public String confirmOutbound(
            @PathVariable("handover_id") Integer handoverId,
            @RequestParam("stagedCodes") List<String> codes,
            RedirectAttributes ra) {
        try {
            warehouseOutboundService.confirmOutbound(handoverId, codes, 1); // Mock executedBy = 1
            ra.addFlashAttribute(SUCCESS_MSG, "Giao dịch thành công. Đã tạo phiếu xuất kho cho " + codes.size() + " tài sản.");
            return REDIRECT_OUTBOUND_DETAIL + handoverId;
        } catch (Exception e) {
            ra.addFlashAttribute(ERROR_MSG, "Lỗi khi xác nhận xuất kho: " + e.getMessage());
            ra.addAttribute("codes", codes);
            return "redirect:/wh/outbound/" + handoverId + "/process";
        }
    }

    @GetMapping("/receipt/{receipt_id}")
    public String viewReceiptDetail(
            @PathVariable("receipt_id") Integer receiptId,
            Model model) {
        try {
            var detail = warehouseOutboundService.getReceiptDetail(receiptId);
            model.addAttribute(ACTIVE_MENU, OUTBOUND);
            model.addAttribute(PAGE_TITLE, "Chi tiết Phiếu xuất " + detail.getReceiptNo());
            model.addAttribute("receipt", detail);
            return "warehouse/outbound/receipt_detail";
        } catch (Exception e) {
            return REDIRECT_OUTBOUND_LIST;
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

    @PostMapping("/{handover_id}/complete")
    public String completeOutbound(
            @PathVariable("handover_id") Integer handoverId,
            RedirectAttributes ra) {
        
        ra.addFlashAttribute(SUCCESS_MSG, "Lệnh xuất kho #" + handoverId + " đã được hoàn tất thành công (giả định).");
        return REDIRECT_OUTBOUND_LIST;
    }
}
