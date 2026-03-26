package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.request.warehouse.InboundRequestDTO;
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
import edu.fpt.groupfive.model.warehouse.WhReceipt;

@Controller
@RequestMapping("/wh/inbound")
@RequiredArgsConstructor
public class WarehouseInboundController {

    private static final String SUCCESS_MSG = "successMessage";
    private static final String ACTIVE_MENU = "activeMenu";
    private static final String MENU_INBOUND = "inbound";
    private static final String PAGE_TITLE = "pageTitle";

    private final OrderService orderService;
    private final WarehouseInboundService warehouseInboundService;

    // =========================================================
    // PO LIST — GET /wh/inbound/po
    // =========================================================

    @GetMapping("/po")
    public String poListPage(Model model) {
        model.addAttribute(ACTIVE_MENU, MENU_INBOUND);
        model.addAttribute(PAGE_TITLE, "Nhập kho PO - Warehouse");

        // Fetch real inbound POs (PENDING, PARTIALLY_RECEIVED, COMPLETED)
        List<PurchaseOrderResponse> orders = orderService.getInboundOrders();

        model.addAttribute("pos", orders);
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

        List<WhReceipt> receipts = warehouseInboundService.getReceiptsByPOId(poId);
        model.addAttribute("receipts", receipts);

        return "warehouse/inbound/po_detail";
    }

    // =========================================================
    // INBOUND FORM — GET /wh/inbound/po/{po_id}/create
    // =========================================================

    @GetMapping("/po/{po_id}/create")
    public String poInboundFormPage(@PathVariable("po_id") Integer poId, Model model) {
        model.addAttribute(ACTIVE_MENU, MENU_INBOUND);
        model.addAttribute(PAGE_TITLE, "Lập phiếu Nhập kho PO #" + poId);

        PurchaseOrderResponse poDetail = orderService.getPurchaseOrderById(poId);
        if ("COMPLETED".equals(poDetail.getOrderStatus())) {
            return "redirect:/wh/inbound/po/" + poId;
        }

        model.addAttribute("po", poDetail);
        return "warehouse/inbound/inbound_form";
    }

    // =========================================================
    // CONFIRM PO — POST /wh/inbound/po/{po_id}/confirm
    // =========================================================

    @PostMapping("/po/{po_id}/confirm")
    public String confirmPO(@PathVariable("po_id") Integer poId,
                            @ModelAttribute InboundRequestDTO request,
                            RedirectAttributes ra,
                            Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        request.setPoId(poId);

        try {
            InboundSummaryResponseDTO summary = warehouseInboundService.processInboundPO(request, username);
            ra.addFlashAttribute(SUCCESS_MSG, "Đã nhập kho thành công cho PO #" + poId + ". Mã phiếu nhập: " + summary.getReceiptNo());
            return "redirect:/wh/inbound/receipt/" + summary.getReceiptId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/wh/inbound/po/" + poId;
        }
    }

    // =========================================================
    // RETURN LIST — GET /wh/inbound/return
    // =========================================================

    @GetMapping("/return")
    public String returnListPage(Model model) {
        model.addAttribute(ACTIVE_MENU, MENU_INBOUND);
        model.addAttribute(PAGE_TITLE, "Nhập kho Thu hồi - Warehouse");

        List<HandoverResponseDTO> allReturns = warehouseInboundService.getAllReturns();
        model.addAttribute("allReturns", allReturns);
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
    // RETURN PROCESS (SCANNER) — GET /wh/inbound/return/{handover_id}/process
    // =========================================================

    @GetMapping("/return/{handover_id}/process")
    public String returnProcessPage(
            @PathVariable("handover_id") Integer handoverId,
            @RequestParam(value = "codes", required = false) List<String> codes,
            Model model) {
        HandoverDetailResponseDTO detail = warehouseInboundService.getReturnDetail(handoverId);
        if (detail == null || "COMPLETED".equals(detail.getStatus())) {
            return "redirect:/wh/inbound/return/" + handoverId;
        }

        model.addAttribute(ACTIVE_MENU, MENU_INBOUND);
        model.addAttribute(PAGE_TITLE, "Xử lý Thu hồi #" + handoverId);
        model.addAttribute("handover", detail);

        if (codes != null && !codes.isEmpty()) {
            model.addAttribute("stagedAssets", warehouseInboundService.getAssetsByCodes(codes));
            model.addAttribute("stagedCodes", codes);
        } else {
            model.addAttribute("stagedAssets", java.util.Collections.emptyList());
            model.addAttribute("stagedCodes", java.util.Collections.emptyList());
        }
        return "warehouse/inbound/return_process";
    }

    // =========================================================
    // STAGE SCAN — POST /wh/inbound/return/{handover_id}/stage-scan
    // =========================================================

    @PostMapping("/return/{handover_id}/stage-scan")
    public String stageReturnScan(
            @PathVariable("handover_id") Integer handoverId,
            @RequestParam("assetCode") String assetCode,
            @RequestParam(value = "stagedCodes", required = false) List<String> currentCodes,
            RedirectAttributes ra) {

        List<String> codes = currentCodes != null ? new java.util.ArrayList<>(currentCodes) : new java.util.ArrayList<>();

        try {
            var asset = warehouseInboundService.validateAssetForReturnInbound(assetCode.trim(), handoverId, codes);
            codes.add(assetCode.trim());
            ra.addFlashAttribute(SUCCESS_MSG, "Đã thêm tài sản '" + asset.getAssetName() + "' (#" + asset.getAssetId() + ") vào danh sách.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        ra.addAttribute("codes", codes);
        return "redirect:/wh/inbound/return/" + handoverId + "/process";
    }

    // =========================================================
    // REMOVE STAGED ITEM — POST /wh/inbound/return/{handover_id}/remove-staged
    // =========================================================

    @PostMapping("/return/{handover_id}/remove-staged")
    public String removeStagedReturnItem(
            @PathVariable("handover_id") Integer handoverId,
            @RequestParam("assetCode") String assetCode,
            @RequestParam(value = "stagedCodes", required = false) List<String> currentCodes,
            RedirectAttributes ra) {

        List<String> codes = currentCodes != null ? new java.util.ArrayList<>(currentCodes) : new java.util.ArrayList<>();
        codes.remove(assetCode);
        ra.addAttribute("codes", codes);
        return "redirect:/wh/inbound/return/" + handoverId + "/process";
    }

    // =========================================================
    // CONFIRM INBOUND — POST /wh/inbound/return/{handover_id}/confirm-inbound
    // =========================================================

    @PostMapping("/return/{handover_id}/confirm-inbound")
    public String confirmReturnInbound(
            @PathVariable("handover_id") Integer handoverId,
            @RequestParam("stagedCodes") List<String> codes,
            Principal principal,
            RedirectAttributes ra) {
        try {
            warehouseInboundService.confirmReturnInbound(handoverId, codes, principal.getName());
            ra.addFlashAttribute(SUCCESS_MSG, "Đã tạo phiếu nhập kho thu hồi thành công cho " + codes.size() + " tài sản.");
            return "redirect:/wh/inbound/return/" + handoverId;
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Lỗi xác nhận nhập kho: " + e.getMessage());
            ra.addAttribute("codes", codes);
            return "redirect:/wh/inbound/return/" + handoverId + "/process";
        }
    }

    @GetMapping("/receipt/{receiptId}")
    public String receiptSummaryPage(@PathVariable("receiptId") Integer receiptId, Model model) {
        model.addAttribute(ACTIVE_MENU, MENU_INBOUND);
        model.addAttribute(PAGE_TITLE, "Chi tiết Phiếu nhập kho #" + receiptId);

        InboundSummaryResponseDTO summary = warehouseInboundService.getReceiptSummary(receiptId);
        model.addAttribute("summary", summary);
        model.addAttribute("isHistory", true);

        return "warehouse/inbound/summary";
    }
}
