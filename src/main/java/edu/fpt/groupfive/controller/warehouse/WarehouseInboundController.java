package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.HandoverResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.InboundSummaryResponseDTO;
import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;
import edu.fpt.groupfive.service.OrderService;
import edu.fpt.groupfive.service.warehouse.WarehouseInboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;

import java.util.List;

@Controller
@RequestMapping("/wh/inbound")
@RequiredArgsConstructor
public class WarehouseInboundController {

    private static final String SUCCESS_MSG = "successMessage";
    private static final String ACTIVE_MENU = "activeMenu";
    private static final String MENU_INBOUND = "inbound";
    private static final String PAGE_TITLE = "pageTitle";
    private static final String SUMMARY_ATTR = "summary";

    private final OrderService orderService;
    private final WarehouseInboundService warehouseInboundService;

    // =========================================================
    // PO LIST — GET /wh/inbound/po
    // =========================================================

    @GetMapping("/po")
    public String poListPage(Model model) {
        model.addAttribute(ACTIVE_MENU, MENU_INBOUND);
        model.addAttribute(PAGE_TITLE, "Nhập kho PO - Warehouse");

        // Fetch real pending POs
        List<PurchaseOrderResponse> pendingOrders = orderService.getOrderWithPending();

        model.addAttribute("pos", pendingOrders);
        return "warehouse/inbound/po_list";
    }

    // =========================================================
    // PO DETAIL — GET /wh/inbound/po/{po_id}
    // =========================================================

    @GetMapping("/po/{po_id}")
    public String poDetailPage(@PathVariable("po_id") Integer poId, Model model) {
        model.addAttribute(ACTIVE_MENU, MENU_INBOUND);
        model.addAttribute(PAGE_TITLE, "Chi tiết Nhận hàng PO #" + poId);

        PurchaseOrderResponse poDetail = orderService.getPurchaseOrderById(poId);
        model.addAttribute("po", poDetail);

        return "warehouse/inbound/po_detail";
    }

    // =========================================================
    // CONFIRM PO — POST /wh/inbound/po/{po_id}/confirm
    // =========================================================

    @PostMapping("/po/{po_id}/confirm")
    public String confirmPO(@PathVariable("po_id") Integer poId, RedirectAttributes ra,
            Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();

        try{
            InboundSummaryResponseDTO summary = warehouseInboundService.processInboundPO(poId, username);
            ra.addFlashAttribute(SUMMARY_ATTR, summary);
            ra.addFlashAttribute(SUCCESS_MSG, "Đã tạo tài sản và nhập kho thành công cho PO #" + poId);
            return "redirect:/wh/inbound/po/" + poId + "/summary";
        }catch (Exception e){
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/wh/inbound/po/" + poId;
        }

    }

    // =========================================================
    // INBOUND SUMMARY — GET /wh/inbound/po/{po_id}/summary
    // =========================================================

    @GetMapping("/po/{po_id}/summary")
    public String poSummaryPage(@PathVariable("po_id") Integer poId, Model model) {
        model.addAttribute(ACTIVE_MENU, MENU_INBOUND);
        model.addAttribute(PAGE_TITLE, "Kết quả Nhập kho PO #" + poId);

        if (!model.containsAttribute(SUMMARY_ATTR)) {
            return "redirect:/wh/inbound/po";
        }

        return "warehouse/inbound/summary";
    }

    // =========================================================
    // RETURN LIST — GET /wh/inbound/return
    // =========================================================

    @GetMapping("/return")
    public String returnListPage(Model model) {
        model.addAttribute(ACTIVE_MENU, MENU_INBOUND);
        model.addAttribute(PAGE_TITLE, "Nhập kho Thu hồi - Warehouse");

        List<HandoverResponseDTO> pendingReturns = warehouseInboundService.getPendingReturns();
        List<HandoverResponseDTO> processedReturns = warehouseInboundService.getProcessedReturns();

        model.addAttribute("returns", pendingReturns);
        model.addAttribute("processedReturns", processedReturns);
        return "warehouse/inbound/return_list";
    }

    // =========================================================
    // RETURN DETAIL — GET /wh/inbound/return/{handover_id}
    // =========================================================

    @GetMapping("/return/{handover_id}")
    public String returnDetailPage(@PathVariable("handover_id") Integer handoverId, Model model) {
        model.addAttribute(ACTIVE_MENU, MENU_INBOUND);
        model.addAttribute(PAGE_TITLE, "Chi tiết Thu hồi #" + handoverId);

        HandoverDetailResponseDTO detail = warehouseInboundService.getReturnDetail(handoverId);
        model.addAttribute("handover", detail);

        return "warehouse/inbound/return_detail";
    }

    // =========================================================
    // PROCESS RETURN SCAN — POST /wh/inbound/return/{handover_id}/scan
    // =========================================================

    @PostMapping("/return/{handover_id}/scan")
    public String processReturnScan(@PathVariable("handover_id") Integer handoverId,
                                    @RequestParam("assetCode") String assetCode,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        try {
            warehouseInboundService.processReturnScan(handoverId, assetCode, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đã quét và nhận kho tài sản thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/wh/inbound/return/" + handoverId;
    }

}
