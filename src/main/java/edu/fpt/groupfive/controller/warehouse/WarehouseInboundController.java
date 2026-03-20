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

import java.time.LocalDateTime;
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
    // =========================================================

    @PostMapping("/po/{po_id}/receive/{po_detail_id}")
    public String receivePO(
            @PathVariable("po_id") Integer poId,
            @PathVariable("po_detail_id") Integer poDetailId,
            @Valid @ModelAttribute("receiveRequest") InboundPOReceiveRequestDTO dto,
            BindingResult result, RedirectAttributes ra) {

        if (result.hasErrors()) {
            ra.addFlashAttribute(ERROR_MSG, "Dữ liệu nhập hàng không hợp lệ.");
            return "redirect:/wh/inbound/po/" + poId;
        }

        ra.addFlashAttribute(SUCCESS_MSG, "Đã ghi nhận nhập kho thành công (demo).");
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
                .purchaseOrderId(101).supplierName("Phong Vũ IT").totalAmount(55000000L)
                .createdAt(LocalDateTime.now().minusDays(2)).status("APPROVED").build(),
            POResponseDTO.builder()
                .purchaseOrderId(102).supplierName("Trần Anh Computer").totalAmount(120000000L)
                .createdAt(LocalDateTime.now().minusDays(5)).status("APPROVED").build()
        );
    }

    private PODetailResponseDTO buildDummyPODetail(Integer poId) {
        return PODetailResponseDTO.builder()
            .purchaseOrderId(poId)
            .supplierName("Nhà cung cấp demo")
            .status("APPROVED")
            .items(List.of(
                POItemDetailDTO.builder().assetTypeId(1).assetTypeName("Laptop Dell XPS").quantity(5).receivedQuantity(0).build()
            ))
            .build();
    }
}
