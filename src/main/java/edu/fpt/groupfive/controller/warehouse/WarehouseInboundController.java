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

import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/warehouse/inbound")
@RequiredArgsConstructor
public class WarehouseInboundController {

    private static final String REDIRECT_PO_LIST = "redirect:/warehouse/inbound/po";
    private static final String SUCCESS_MSG = "successMessage";
    private static final String ERROR_MSG = "errorMessage";

    private final edu.fpt.groupfive.service.OrderService orderService;
    private final edu.fpt.groupfive.service.warehouse.WhInboundService whInboundService;
    private final edu.fpt.groupfive.service.UserService userService;

    // =========================================================
    //  PO LIST  —  GET /warehouse/inbound/po
    // =========================================================

    @GetMapping("/po")
    public String poListPage(Model model) {
        model.addAttribute("activeMenu", "inbound");
        model.addAttribute("pageTitle", "Nhập kho PO - Warehouse");
        
        List<PurchaseOrderResponse> allPOs = orderService.getPendingInboundPOs();
        
        List<POResponseDTO> poList = allPOs.stream()
                .map(this::mapToPOResponseDTO)
                .collect(Collectors.toList());
                
        model.addAttribute("pos", poList);
        return "warehouse/inbound/po-list";
    }

    // =========================================================
    //  PO DETAIL  —  GET /warehouse/inbound/po/{poId}
    // =========================================================

    @GetMapping("/po/{poId}")
    public String poDetailPage(@PathVariable("poId") Integer poId, Model model) {
        model.addAttribute("activeMenu", "inbound");
        model.addAttribute("pageTitle", "Chi tiết Nhận hàng PO #" + poId);
        
        edu.fpt.groupfive.dto.response.PurchaseOrderResponse poResponse = orderService.getPurchaseOrderById(poId);
        PODetailResponseDTO poDetail = mapToPODetailResponseDTO(poResponse);
        model.addAttribute("po", poDetail);
        
        if (!model.containsAttribute("receiveRequest")) {
            InboundPOReceiveRequestDTO request = new InboundPOReceiveRequestDTO();
            request.setPurchaseOrderId(poId);
            model.addAttribute("receiveRequest", request);
        }
        
        return "warehouse/inbound/po-detail";
    }

    // =========================================================
    //  PROCESS RECEIVE  —  POST /warehouse/inbound/po/receive
    // =========================================================

    @PostMapping("/po/receive")
    public String receivePO(
            @Valid @ModelAttribute("receiveRequest") InboundPOReceiveRequestDTO dto,
            BindingResult result, java.security.Principal principal, RedirectAttributes ra) {

        if (result.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.receiveRequest", result);
            ra.addFlashAttribute("receiveRequest", dto);
            ra.addFlashAttribute(ERROR_MSG, "Dữ liệu nhập hàng không hợp lệ.");
            return "redirect:/warehouse/inbound/po/" + dto.getPurchaseOrderId();
        }

        try {
            Integer userId = userService.getUserIdByUsername(principal.getName());
            whInboundService.processPOInbound(dto, userId);
            ra.addFlashAttribute(SUCCESS_MSG, "Đã ghi nhận nhập kho thành công cho PO #" + dto.getPurchaseOrderId());
        } catch (Exception e) {
            ra.addFlashAttribute(ERROR_MSG, "Lỗi khi xử lý nhập kho: " + e.getMessage());
            return "redirect:/warehouse/inbound/po/" + dto.getPurchaseOrderId();
        }
        
        return REDIRECT_PO_LIST;
    }

    // =========================================================
    //  MAPPING HELPERS
    // =========================================================

    private POResponseDTO mapToPOResponseDTO(PurchaseOrderResponse po) {
        return POResponseDTO.builder()
                .purchaseOrderId(po.getOrderId())
                .supplierName(po.getSupplierName())
                .totalAmount(po.getTotalAmount() != null ? po.getTotalAmount().longValue() : 0L)
                .createdAt(po.getCreatedAt())
                .status(po.getOrderStatus())
                .build();
    }

    private PODetailResponseDTO mapToPODetailResponseDTO(edu.fpt.groupfive.dto.response.PurchaseOrderResponse po) {
        List<POItemDetailDTO> items = po.getOrderDetails().stream()
                .map(d -> POItemDetailDTO.builder()
                        .purchaseOrderDetailId(d.getPurchaseOrderDetailId())
                        .assetTypeId(d.getAssetTypeId())
                        .assetTypeName(d.getAssetTypeName())
                        .quantity(d.getQuantity())
                        .receivedQuantity(0) // Logic thật cần check mapping po -> transactions -> count assets
                        .build())
                .toList();

        return PODetailResponseDTO.builder()
                .purchaseOrderId(po.getOrderId())
                .supplierName(po.getSupplierName())
                .status(po.getOrderStatus())
                .items(items)
                .build();
    }
}
