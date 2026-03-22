package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.dto.response.AllocationRequestResponse;
import edu.fpt.groupfive.dto.response.warehouse.HandoverDetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.QCReportRequestDTO;
import edu.fpt.groupfive.service.warehouse.WarehouseOutboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
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
    private static final String PENDING = "PENDING";
    private static final String DEPT_DAO_TAO = "Phòng Đào tạo";
    private static final String REDIRECT_OUTBOUND = "redirect:/wh/outbound/";
    private static final String AST_PRINTER = "Máy in HP";
    private static final String AST_PHOTOCOPY = "Máy photocopy";
    private static final String CODE_PRINTER = "AST-881";
    private static final String CODE_PHOTOCOPY = "AST-882";
    
    private final WarehouseOutboundService warehouseOutboundService;

    // Mock State Management for Demo
    private static final List<HandoverDetailResponseDTO.HandoverItemDTO> SELECTED_ASSETS = new ArrayList<>();

    @GetMapping("")
    public String allocationListPage(Model model) {
        model.addAttribute(ACTIVE_MENU, OUTBOUND);
        model.addAttribute(PAGE_TITLE, "Xuất kho Cấp phát - Warehouse");
        model.addAttribute("allocations", warehouseOutboundService.getPendingAllocations());
        return "warehouse/outbound/allocation_list";
    }

    @GetMapping("/{handover_id}")
    public String allocationDetailPage(@PathVariable("handover_id") Integer handoverId, Model model) {
        model.addAttribute(ACTIVE_MENU, OUTBOUND);
        model.addAttribute(PAGE_TITLE, "Chi tiết Cấp phát #" + handoverId);

        // Mock Allocation Request
        AllocationRequestResponse allocationRequest = AllocationRequestResponse.builder()
                .requestId(101).userName("Nguyễn Văn A").requestedDepartmentName(DEPT_DAO_TAO)
                .requestReason("Cấp phát thiết bị cho giảng viên mới").priority(Priority.HIGH)
                .neededByDate(LocalDate.now().plusDays(3)).status("APPROVED").build();

        List<HandoverDetailResponseDTO.RequestedItemDTO> requestedItems = List.of(
                HandoverDetailResponseDTO.RequestedItemDTO.builder().assetTypeName(AST_PRINTER).requestedQuantity(1).allocatedQuantity(
                        (int) SELECTED_ASSETS.stream().filter(a -> a.getAssetTypeName().equals(AST_PRINTER)).count()
                ).build(),
                HandoverDetailResponseDTO.RequestedItemDTO.builder().assetTypeName(AST_PHOTOCOPY).requestedQuantity(1).allocatedQuantity(
                        (int) SELECTED_ASSETS.stream().filter(a -> a.getAssetTypeName().equals(AST_PHOTOCOPY)).count()
                ).build()
        );

        model.addAttribute("handover", HandoverDetailResponseDTO.builder()
            .handoverId(handoverId).fromDepartmentName("Kho Tổng").toDepartmentName(DEPT_DAO_TAO)
            .status(PENDING).allocationRequest(allocationRequest).requestedItems(requestedItems)
            .items(new ArrayList<>(SELECTED_ASSETS)).build());
            
        return "warehouse/outbound/allocation_detail";
    }

    private boolean isAllItemsAllocated() {
        // Mock requirements: 1 Printer, 1 Photocopy
        long printerCount = SELECTED_ASSETS.stream().filter(a -> a.getAssetTypeName().equals(AST_PRINTER)).count();
        long photocopyCount = SELECTED_ASSETS.stream().filter(a -> a.getAssetTypeName().equals(AST_PHOTOCOPY)).count();
        return printerCount >= 1 && photocopyCount >= 1;
    }

    @PostMapping("/{handover_id}/scan")
    public String processAllocationScan(
            @PathVariable("handover_id") Integer handoverId,
            @RequestParam("assetCode") String assetCode,
            RedirectAttributes ra) {

        // Mock finding asset
        if (assetCode.equals(CODE_PRINTER) || assetCode.equals(CODE_PHOTOCOPY)) {
            if (SELECTED_ASSETS.stream().anyMatch(a -> a.getAssetCode().equals(assetCode))) {
                ra.addFlashAttribute(ERROR_MSG, "Tài sản này đã được thêm.");
                return REDIRECT_OUTBOUND + handoverId;
            }
            return REDIRECT_OUTBOUND + handoverId + "/qc-report?assetCode=" + assetCode;
        }

        ra.addFlashAttribute(ERROR_MSG, "Mã tài sản không hợp lệ hoặc không có trong kho.");
        return REDIRECT_OUTBOUND + handoverId;
    }

    @GetMapping("/{handover_id}/qc-report")
    public String qcReportPage(
            @PathVariable("handover_id") Integer handoverId,
            @RequestParam("assetCode") String assetCode,
            Model model) {
        
        model.addAttribute(ACTIVE_MENU, OUTBOUND);
        model.addAttribute(PAGE_TITLE, "Báo cáo chất lượng " + assetCode);

        String assetTypeName = assetCode.equals(CODE_PRINTER) ? AST_PRINTER : AST_PHOTOCOPY;
        Integer assetId = assetCode.equals(CODE_PRINTER) ? 20 : 21;

        QCReportRequestDTO.QCItemDTO item = QCReportRequestDTO.QCItemDTO.builder()
                .assetId(assetId).assetCode(assetCode).assetTypeName(assetTypeName).condition("GOOD").build();

        model.addAttribute("qcReport", QCReportRequestDTO.builder()
                .handoverId(handoverId).items(List.of(item)).build());

        return "warehouse/outbound/qc_report";
    }

    @PostMapping("/{handover_id}/submit-qc")
    public String submitQCReport(
            @PathVariable("handover_id") Integer handoverId,
            @ModelAttribute QCReportRequestDTO qcReport,
            RedirectAttributes ra) {

        QCReportRequestDTO.QCItemDTO item = qcReport.getItems().get(0);
        
        // Add to selected assets list
        SELECTED_ASSETS.add(HandoverDetailResponseDTO.HandoverItemDTO.builder()
                .assetId(item.getAssetId()).assetCode(item.getAssetCode())
                .assetTypeName(item.getAssetTypeName()).isScanned(true).build());

        ra.addFlashAttribute(SUCCESS_MSG, "Đã quét và xuất kho tài sản " + item.getAssetCode());
        
        if (isAllItemsAllocated()) {
            SELECTED_ASSETS.clear();
            ra.addFlashAttribute(SUCCESS_MSG, "Đã hoàn tất toàn bộ yêu cầu cấp phát.");
            return "redirect:/wh/outbound";
        }
        
        return REDIRECT_OUTBOUND + handoverId;
    }

}
