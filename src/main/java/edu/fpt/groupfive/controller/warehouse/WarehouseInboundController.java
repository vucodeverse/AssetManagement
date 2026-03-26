package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.request.warehouse.InboundRequestDTO;
import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.HandoverResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.InboundSummaryResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.InboundReceiptResponseDTO;
import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;
import edu.fpt.groupfive.service.OrderService;
import edu.fpt.groupfive.service.warehouse.WarehouseInboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/wh/inbound")
@RequiredArgsConstructor
public class WarehouseInboundController {

    private static final String SUCCESS_MSG = "successMessage";
    private static final String ACTIVE_MENU = "activeMenu";
    private static final String MENU_INBOUND = "inbound";
    private static final String PAGE_TITLE = "pageTitle";
    private static final String PENDING_STATUS = "PENDING";
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

    @GetMapping("/receipts")
    public String receiptListPage(Model model) {
        model.addAttribute(ACTIVE_MENU, MENU_INBOUND);
        model.addAttribute(PAGE_TITLE, "Lịch sử Phiếu Nhập kho");
        
        List<InboundReceiptResponseDTO> receipts = warehouseInboundService.getAllInboundReceipts();
        model.addAttribute("receipts", receipts);
        
        return "warehouse/inbound/receipt_list";
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
        model.addAttribute("receipts", warehouseInboundService.getReceiptsByOrderId(poId));

        return "warehouse/inbound/po_detail";
    }

    // =========================================================
    // CONFIRM PO — POST /wh/inbound/po/{po_id}/confirm
    // =========================================================

    @PostMapping("/po/{po_id}/confirm")
    public String confirmPO(@PathVariable("po_id") Integer poId, 
            @ModelAttribute InboundRequestDTO inboundRequest,
            RedirectAttributes ra,
            java.security.Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        inboundRequest.setPurchaseOrderId(poId);

        try {
            InboundSummaryResponseDTO summary = warehouseInboundService.processInboundPO(inboundRequest, username);
            ra.addFlashAttribute(SUMMARY_ATTR, summary);
            ra.addFlashAttribute(SUCCESS_MSG, "Đã tạo tài sản và nhập kho thành công cho PO #" + poId);
            return "redirect:/wh/inbound/po/" + poId + "/summary";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
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
            return "redirect:/wh/inbound/po/" + poId;
        }

        return "warehouse/inbound/summary";
    }

    @GetMapping("/receipt/{receipt_id}")
    public String viewReceipt(@PathVariable("receipt_id") Integer receiptId, Model model) {
        model.addAttribute(ACTIVE_MENU, MENU_INBOUND);
        
        InboundSummaryResponseDTO summary = warehouseInboundService.getInboundReceiptSummary(receiptId);
        if (summary == null) {
            return "redirect:/wh/inbound/po";
        }
        
        model.addAttribute(SUMMARY_ATTR, summary);
        model.addAttribute(PAGE_TITLE, "Thông tin Phiếu Nhập #" + receiptId);
        return "warehouse/inbound/summary";
    }

    // =========================================================
    // RETURN LIST — GET /wh/inbound/return
    // =========================================================

    @GetMapping("/return")
    public String returnListPage(Model model) {
        model.addAttribute(ACTIVE_MENU, MENU_INBOUND);
        model.addAttribute(PAGE_TITLE, "Nhập kho Thu hồi - Warehouse");
        model.addAttribute("returns", List.of(
                HandoverResponseDTO.builder().handoverId(501).fromDepartmentName("Phòng IT")
                        .createdAt(LocalDateTime.now().minusHours(5)).status(PENDING_STATUS).build(),
                HandoverResponseDTO.builder().handoverId(502).fromDepartmentName("Phòng HR")
                        .createdAt(LocalDateTime.now().minusDays(1)).status(PENDING_STATUS).build()));
        return "warehouse/inbound/return_list";
    }

    // =========================================================
    // RETURN DETAIL — GET /wh/inbound/return/{handover_id}
    // =========================================================

    @GetMapping("/return/{handover_id}")
    public String returnDetailPage(@PathVariable("handover_id") Integer handoverId, Model model) {
        model.addAttribute(ACTIVE_MENU, MENU_INBOUND);
        model.addAttribute(PAGE_TITLE, "Chi tiết Thu hồi #" + handoverId);
        model.addAttribute("handover", HandoverDetailResponseDTO.builder()
                .handoverId(handoverId).fromDepartmentName("Phòng IT").status(PENDING_STATUS)
                .items(List.of(
                        HandoverDetailResponseDTO.HandoverItemDTO.builder().assetId(10).assetCode("AST-991")
                                .assetTypeName("Laptop Dell").isScanned(false).build(),
                        HandoverDetailResponseDTO.HandoverItemDTO.builder().assetId(11).assetCode("AST-992")
                                .assetTypeName("Chuột Logitech").isScanned(false).build()))
                .build());
        return "warehouse/inbound/return_detail";
    }

    // =========================================================
    // PROCESS RETURN SCAN — POST /wh/inbound/return/{handover_id}/scan
    // =========================================================

    @PostMapping("/return/{handover_id}/scan")
    public String processReturnScan(
            @PathVariable("handover_id") Integer handoverId,
            @RequestParam("assetCode") String assetCode,
            RedirectAttributes ra) {

        ra.addFlashAttribute(SUCCESS_MSG, "Đã nhận tài sản " + assetCode + " (demo).");
        return "redirect:/wh/inbound/return/" + handoverId;
    }

}
