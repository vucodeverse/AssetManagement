package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.request.warehouse.InboundPOReceiveRequestDTO;
import edu.fpt.groupfive.dto.response.warehouse.PODetailResponseDTO;
import edu.fpt.groupfive.dto.response.warehouse.POItemDetailDTO;
import edu.fpt.groupfive.dto.response.warehouse.POResponseDTO;
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
@RequestMapping("/warehouse/inbound")
@RequiredArgsConstructor
public class WarehouseInboundController {

    private static final String REDIRECT_PO_LIST = "redirect:/warehouse/inbound/po";
    private static final String SUCCESS_MSG = "successMessage";
    private static final String ERROR_MSG = "errorMessage";

    // =========================================================
    //  PO LIST  —  GET /warehouse/inbound/po
    // =========================================================

    @GetMapping("/po")
    public String poListPage(Model model) {
        model.addAttribute("activeMenu", "inbound");
        model.addAttribute("pageTitle", "Nhập kho PO - Warehouse");
        model.addAttribute("pos", buildDummyPOs());
        return "warehouse/inbound/po-list";
    }

    // =========================================================
    //  PO DETAIL  —  GET /warehouse/inbound/po/{poId}
    // =========================================================

    @GetMapping("/po/{poId}")
    public String poDetailPage(@PathVariable("poId") Integer poId, Model model) {
        model.addAttribute("activeMenu", "inbound");
        model.addAttribute("pageTitle", "Chi tiết Nhận hàng PO #" + poId);
        
        PODetailResponseDTO poDetail = buildDummyPODetail(poId);
        model.addAttribute("po", poDetail);
        
        if (!model.containsAttribute("receiveRequest")) {
            model.addAttribute("receiveRequest", new InboundPOReceiveRequestDTO());
        }
        
        return "warehouse/inbound/po-detail";
    }

    // =========================================================
    //  PROCESS RECEIVE  —  POST /warehouse/inbound/po/receive
    // =========================================================

    @PostMapping("/po/receive")
    public String receivePO(
            @Valid @ModelAttribute("receiveRequest") InboundPOReceiveRequestDTO dto,
            BindingResult result, RedirectAttributes ra) {

        if (result.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.receiveRequest", result);
            ra.addFlashAttribute("receiveRequest", dto);
            ra.addFlashAttribute(ERROR_MSG, "Dữ liệu nhập hàng không hợp lệ.");
            return "redirect:/warehouse/inbound/po/" + dto.getPurchaseOrderId();
        }

        // Logic thật sẽ tạo Asset record, Transaction, Update Zone capacity...
        ra.addFlashAttribute(SUCCESS_MSG, "Đã ghi nhận nhập kho cho PO #" + dto.getPurchaseOrderId() + " (demo).");
        return REDIRECT_PO_LIST;
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
                .createdAt(LocalDateTime.now().minusDays(5)).status("APPROVED").build(),
            POResponseDTO.builder()
                .purchaseOrderId(105).supplierName("Dell Global Vietnam").totalAmount(210000000L)
                .createdAt(LocalDateTime.now().minusDays(1)).status("APPROVED").build()
        );
    }

    private PODetailResponseDTO buildDummyPODetail(Integer poId) {
        return PODetailResponseDTO.builder()
            .purchaseOrderId(poId)
            .supplierName(poId == 101 ? "Phong Vũ IT" : "Nhà cung cấp khác")
            .status("APPROVED")
            .items(List.of(
                POItemDetailDTO.builder().assetTypeId(1).assetTypeName("Laptop Dell XPS").quantity(5).receivedQuantity(0).build(),
                POItemDetailDTO.builder().assetTypeId(2).assetTypeName("Màn hình LG 24\"").quantity(10).receivedQuantity(0).build()
            ))
            .build();
    }
}
