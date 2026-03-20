package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.HandoverResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.PODetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.POItemDetailDTO;
import edu.fpt.groupfive.dto.response.warehouse.POResponseDTO;
import edu.fpt.groupfive.dto.request.warehouse.InboundPOReceiveRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.fpt.groupfive.dto.response.warehouse.BarcodeDistributionResponseDTO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/wh/inbound")
@RequiredArgsConstructor
public class WarehouseInboundController {

    private static final String REDIRECT_PO_LIST = "redirect:/wh/inbound/po";
    private static final String SUCCESS_MSG = "successMessage";
    private static final String ERROR_MSG = "errorMessage";

    // =========================================================
    //  PO LIST  —  GET /wh/inbound/po
    // =========================================================

    @GetMapping("/po")
    public String poListPage(Model model) {
        model.addAttribute("activeMenu", "inbound");
        model.addAttribute("pageTitle", "Nhập kho PO - Warehouse");
        model.addAttribute("pos", buildDummyPOs());
        return "warehouse/inbound/po_list";
    }

    // =========================================================
    //  PO DETAIL  —  GET /wh/inbound/po/{po_id}
    // =========================================================

    @GetMapping("/po/{po_id}")
    public String poDetailPage(@PathVariable("po_id") Integer poId, Model model) {
        model.addAttribute("activeMenu", "inbound");
        model.addAttribute("pageTitle", "Chi tiết Nhận hàng PO #" + poId);
        
        PODetailResponseDTO poDetail = buildDummyPODetail(poId);
        model.addAttribute("po", poDetail);
        
        if (!model.containsAttribute("receiveRequest")) {
            model.addAttribute("receiveRequest", new InboundPOReceiveRequestDTO());
        }
        
        return "warehouse/inbound/po_detail";
    }

    // =========================================================
    //  PROCESS RECEIVE  —  POST /wh/inbound/po/{po_id}/receive/{po_detail_id}
    //  NEW: Redirects to barcode distribution instead of finishing
    // =========================================================

    @PostMapping("/po/{po_id}/receive/{po_detail_id}")
    public String receivePO(
            @PathVariable("po_id") Integer poId,
            @PathVariable("po_detail_id") Integer poDetailId,
            @RequestParam("actualQuantity") Integer actualQuantity,
            RedirectAttributes ra) {

        if (actualQuantity == null || actualQuantity <= 0) {
            ra.addFlashAttribute(ERROR_MSG, "Số lượng thực nhập không hợp lệ.");
            return "redirect:/wh/inbound/po/" + poId;
        }

        // Logic sync: Instead of finishing, go to barcode distribution
        return "redirect:/wh/inbound/po/" + poId + "/barcode?typeId=" + poDetailId + "&qty=" + actualQuantity;
    }

    // =========================================================
    //  BARCODE DISTRIBUTION — GET /wh/inbound/po/{po_id}/barcode
    // =========================================================

    @GetMapping("/po/{po_id}/barcode")
    public String barcodeDistributionPage(
            @PathVariable("po_id") Integer poId,
            @RequestParam("typeId") Integer assetTypeId,
            @RequestParam("qty") Integer quantity,
            Model model) {
            
        model.addAttribute("activeMenu", "inbound");
        model.addAttribute("pageTitle", "Phân phối Mã định danh - PO #" + poId);

        // Build mock items based on quantity received
        List<BarcodeDistributionResponseDTO.BarcodeItemDTO> items = new ArrayList<>();
        for (int i = 1; i <= quantity; i++) {
            items.add(BarcodeDistributionResponseDTO.BarcodeItemDTO.builder()
                    .assetCode("AST-" + poId + "-" + (1000 + i))
                    .status("CHỜ DÁN NHÃN")
                    .build());
        }

        BarcodeDistributionResponseDTO distribution = BarcodeDistributionResponseDTO.builder()
                .purchaseOrderId(poId)
                .assetTypeId(assetTypeId)
                .assetTypeName("Tài sản từ PO #" + poId) // Placeholder
                .quantity(quantity)
                .items(items)
                .build();

        model.addAttribute("distribution", distribution);
        return "warehouse/inbound/barcode_distribution";
    }

    // =========================================================
    //  CONFIRM BARCODE — POST /wh/inbound/po/{po_id}/barcode/confirm
    // =========================================================

    @PostMapping("/po/{po_id}/barcode/confirm")
    public String confirmBarcodeDistribution(@PathVariable("po_id") Integer poId, RedirectAttributes ra) {
        ra.addFlashAttribute(SUCCESS_MSG, "Đã ghi nhận nhập kho và dán mã thành công cho PO #" + poId);
        return "redirect:/wh/inbound/po/" + poId;
    }

    // =========================================================
    //  RETURN LIST  —  GET /wh/inbound/return
    // =========================================================

    @GetMapping("/return")
    public String returnListPage(Model model) {
        model.addAttribute("activeMenu", "inbound");
        model.addAttribute("pageTitle", "Nhập kho Thu hồi - Warehouse");
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
        model.addAttribute("activeMenu", "inbound");
        model.addAttribute("pageTitle", "Chi tiết Thu hồi #" + handoverId);
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
