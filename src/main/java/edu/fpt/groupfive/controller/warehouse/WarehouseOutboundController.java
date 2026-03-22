package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.HandoverResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/wh/outbound")
@RequiredArgsConstructor
public class WarehouseOutboundController {

    private static final String SUCCESS_MSG = "successMessage";

    // =========================================================
    //  ALLOCATION LIST  —  GET /wh/outbound
    // =========================================================
    @GetMapping("")
    public String allocationListPage(Model model) {
        model.addAttribute("activeMenu", "outbound");
        model.addAttribute("pageTitle", "Xuất kho Cấp phát - Warehouse");
        model.addAttribute("allocations", List.of(
            HandoverResponseDTO.builder().handoverId(601).toDepartmentName("Phòng Đào tạo").createdAt(LocalDateTime.now().minusHours(2)).status("PENDING").build(),
            HandoverResponseDTO.builder().handoverId(602).toDepartmentName("Phòng Tuyển sinh").createdAt(LocalDateTime.now().minusDays(1)).status("PENDING").build()
        ));
        return "warehouse/outbound/allocation_list";
    }

    // =========================================================
    //  ALLOCATION DETAIL  —  GET /wh/outbound/{handover_id}
    // =========================================================
    @GetMapping("/{handover_id}")
    public String allocationDetailPage(@PathVariable("handover_id") Integer handoverId, Model model) {
        model.addAttribute("activeMenu", "outbound");
        model.addAttribute("pageTitle", "Chi tiết Cấp phát #" + handoverId);
        model.addAttribute("handover", HandoverDetailResponseDTO.builder()
            .handoverId(handoverId).toDepartmentName("Phòng Đào tạo").status("PENDING")
            .items(List.of(
                HandoverDetailResponseDTO.HandoverItemDTO.builder().assetId(20).assetCode("AST-881").assetTypeName("Máy in HP").isScanned(false).build(),
                HandoverDetailResponseDTO.HandoverItemDTO.builder().assetId(21).assetCode("AST-882").assetTypeName("Máy photocopy").isScanned(false).build()
            )).build());
        return "warehouse/outbound/allocation_detail";
    }

    // =========================================================
    //  PROCESS ALLOCATION SCAN  —  POST /wh/outbound/{handover_id}/scan
    // =========================================================
    @PostMapping("/{handover_id}/scan")
    public String processAllocationScan(
            @PathVariable("handover_id") Integer handoverId,
            @RequestParam("assetCode") String assetCode,
            RedirectAttributes ra) {

        ra.addFlashAttribute(SUCCESS_MSG, "Đã xuất tài sản " + assetCode + " (demo).");
        return "redirect:/wh/outbound/" + handoverId;
    }
}
