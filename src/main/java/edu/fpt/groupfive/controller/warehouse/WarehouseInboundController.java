package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.HandoverResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.PODetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.POItemDetailDTO;
import edu.fpt.groupfive.dto.response.warehouse.POResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.InboundSummaryResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    // =========================================================
    //  PO LIST  —  GET /wh/inbound/po
    // =========================================================

    @GetMapping("/po")
    public String poListPage(Model model) {
        model.addAttribute(ACTIVE_MENU, MENU_INBOUND);
        model.addAttribute(PAGE_TITLE, "Nhập kho PO - Warehouse");
        model.addAttribute("pos", buildDummyPOs());
        return "warehouse/inbound/po_list";
    }

    // =========================================================
    //  PO DETAIL  —  GET /wh/inbound/po/{po_id}
    // =========================================================

    @GetMapping("/po/{po_id}")
    public String poDetailPage(@PathVariable("po_id") Integer poId, Model model) {
        model.addAttribute(ACTIVE_MENU, MENU_INBOUND);
        model.addAttribute(PAGE_TITLE, "Chi tiết Nhận hàng PO #" + poId);
        
        PODetailResponseDTO poDetail = buildDummyPODetail(poId);
        model.addAttribute("po", poDetail);
        
        return "warehouse/inbound/po_detail";
    }

    // =========================================================
    //  CONFIRM PO — POST /wh/inbound/po/{po_id}/confirm
    // =========================================================

    @PostMapping("/po/{po_id}/confirm")
    public String confirmPO(@PathVariable("po_id") Integer poId, RedirectAttributes ra) {
        PODetailResponseDTO poDetail = buildDummyPODetail(poId);
        
        List<InboundSummaryResponseDTO.AssetGroupDTO> groups = new ArrayList<>();
        int nextId = 1001; // Base ID for demo
        for (POItemDetailDTO item : poDetail.getItems()) {
            int qty = item.getQuantity() - item.getReceivedQuantity();
            if (qty > 0) {
                List<Integer> ids = new ArrayList<>();
                for (int i = 0; i < qty; i++) {
                    ids.add(nextId++);
                }
                groups.add(InboundSummaryResponseDTO.AssetGroupDTO.builder()
                        .assetTypeName(item.getAssetTypeName())
                        .quantity(qty)
                        .assetIds(ids)
                        .build());
            }
        }
        
        InboundSummaryResponseDTO summary = InboundSummaryResponseDTO.builder()
                .purchaseOrderId(poId)
                .supplierName(poDetail.getSupplierName())
                .inboundDate(LocalDateTime.now())
                .assetGroups(groups)
                .build();
        
        ra.addFlashAttribute(SUMMARY_ATTR, summary);
        ra.addFlashAttribute(SUCCESS_MSG, "Đã dán mã và nhập kho thành công toàn bộ PO #" + poId);
        return "redirect:/wh/inbound/po/" + poId + "/summary";
    }

    // =========================================================
    //  INBOUND SUMMARY — GET /wh/inbound/po/{po_id}/summary
    // =========================================================

    @GetMapping("/po/{po_id}/summary")
    public String poSummaryPage(@PathVariable("po_id") Integer poId, Model model) {
        model.addAttribute(ACTIVE_MENU, MENU_INBOUND);
        model.addAttribute(PAGE_TITLE, "Kết quả Nhập kho PO #" + poId);
        
        if (!model.containsAttribute(SUMMARY_ATTR)) {
             // Fallback for demo if flash attribute is lost
             PODetailResponseDTO poDetail = buildDummyPODetail(poId);
             List<InboundSummaryResponseDTO.AssetGroupDTO> groups = new ArrayList<>();
             int nextId = 2001;
             for (POItemDetailDTO item : poDetail.getItems()) {
                 groups.add(InboundSummaryResponseDTO.AssetGroupDTO.builder()
                         .assetTypeName(item.getAssetTypeName())
                         .quantity(item.getQuantity())
                         .assetIds(List.of(nextId++, nextId++))
                         .build());
             }
             model.addAttribute(SUMMARY_ATTR, InboundSummaryResponseDTO.builder()
                     .purchaseOrderId(poId)
                     .supplierName(poDetail.getSupplierName())
                     .inboundDate(LocalDateTime.now())
                     .assetGroups(groups)
                     .build());
        }
        
        return "warehouse/inbound/summary";
    }

    // =========================================================
    //  RETURN LIST  —  GET /wh/inbound/return
    // =========================================================

    @GetMapping("/return")
    public String returnListPage(Model model) {
        model.addAttribute(ACTIVE_MENU, MENU_INBOUND);
        model.addAttribute(PAGE_TITLE, "Nhập kho Thu hồi - Warehouse");
        model.addAttribute("returns", List.of(
            HandoverResponseDTO.builder().handoverId(501).fromDepartmentName("Phòng IT").createdAt(LocalDateTime.now().minusHours(5)).status("PENDING").build(),
            HandoverResponseDTO.builder().handoverId(502).fromDepartmentName("Phòng HR").createdAt(LocalDateTime.now().minusDays(1)).status("PENDING").build()
        ));
        return "warehouse/inbound/return_list";
    }

    // =========================================================
    //  RETURN DETAIL  —  GET /wh/inbound/return/{handover_id}
    // =========================================================

    @GetMapping("/return/{handover_id}")
    public String returnDetailPage(@PathVariable("handover_id") Integer handoverId, Model model) {
        model.addAttribute(ACTIVE_MENU, MENU_INBOUND);
        model.addAttribute(PAGE_TITLE, "Chi tiết Thu hồi #" + handoverId);
        model.addAttribute("handover", HandoverDetailResponseDTO.builder()
            .handoverId(handoverId).fromDepartmentName("Phòng IT").status("PENDING")
            .items(List.of(
                HandoverDetailResponseDTO.HandoverItemDTO.builder().assetId(10).assetCode("AST-991").assetTypeName("Laptop Dell").isScanned(false).build(),
                HandoverDetailResponseDTO.HandoverItemDTO.builder().assetId(11).assetCode("AST-992").assetTypeName("Chuột Logitech").isScanned(false).build()
            )).build());
        return "warehouse/inbound/return_detail";
    }

    // =========================================================
    //  PROCESS RETURN SCAN  —  POST /wh/inbound/return/{handover_id}/scan
    // =========================================================

    @PostMapping("/return/{handover_id}/scan")
    public String processReturnScan(
            @PathVariable("handover_id") Integer handoverId,
            @RequestParam("assetCode") String assetCode,
            RedirectAttributes ra) {
        
        ra.addFlashAttribute(SUCCESS_MSG, "Đã nhận tài sản " + assetCode + " (demo).");
        return "redirect:/wh/inbound/return/" + handoverId;
    }


    // =========================================================
    //  DUMMY DATA
    // =========================================================

    private List<POResponseDTO> buildDummyPOs() {
        return List.of(
            POResponseDTO.builder()
                .purchaseOrderId(101).supplierName("Phong Vũ IT").totalAmount(155000000L)
                .createdAt(LocalDateTime.now().minusDays(2)).status("APPROVED").build(),
            POResponseDTO.builder()
                .purchaseOrderId(102).supplierName("Trần Anh Computer").totalAmount(120000000L)
                .createdAt(LocalDateTime.now().minusDays(5)).status("APPROVED").build(),
            POResponseDTO.builder()
                .purchaseOrderId(103).supplierName("Hà Nội Computer").totalAmount(45000000L)
                .createdAt(LocalDateTime.now().minusDays(1)).status("APPROVED").build()
        );
    }

    private PODetailResponseDTO buildDummyPODetail(Integer poId) {
        if (poId == 101) {
            return PODetailResponseDTO.builder()
                .purchaseOrderId(101)
                .supplierName("Phong Vũ IT")
                .status("APPROVED")
                .items(List.of(
                    POItemDetailDTO.builder().assetTypeId(1).assetTypeName("Laptop Dell XPS").quantity(5).receivedQuantity(2).build(),
                    POItemDetailDTO.builder().assetTypeId(2).assetTypeName("Màn hình Dell 24 inch").quantity(10).receivedQuantity(0).build(),
                    POItemDetailDTO.builder().assetTypeId(3).assetTypeName("Bàn phím Logitech K120").quantity(20).receivedQuantity(5).build(),
                    POItemDetailDTO.builder().assetTypeId(4).assetTypeName("Chuột không dây Silent").quantity(15).receivedQuantity(10).build()
                ))
                .build();
        }
        
        return PODetailResponseDTO.builder()
            .purchaseOrderId(poId)
            .supplierName("Nhà cung cấp demo")
            .status("APPROVED")
            .items(List.of(
                POItemDetailDTO.builder().assetTypeId(5).assetTypeName("Máy in Canon LBP").quantity(3).receivedQuantity(0).build()
            ))
            .build();
    }
}
