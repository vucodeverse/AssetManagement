package edu.fpt.groupfive.controller.order;

import edu.fpt.groupfive.dto.request.PurchaseOrderCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseOrderDetailCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseOrderSearchCriteria;
import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;
import edu.fpt.groupfive.service.ISupplierService;
import edu.fpt.groupfive.service.OrderService;
import edu.fpt.groupfive.util.OrderCalculationUtil;
import edu.fpt.groupfive.util.annotation.IsDirector;
import edu.fpt.groupfive.util.annotation.IsPurchaseStaff;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/purchase-orders")
public class OrderController {

    private static final String VIEW_ORDER_LIST = "order/order-from-purchase";
    private static final String VIEW_ORDER_FORM = "order/order-form";
    private static final String VIEW_ORDER_DETAIL = "order/order-of-purchase";

    private final OrderService orderService;
    private final ISupplierService supplierService;
    private final OrderCalculationUtil orderCalculationUtil;


    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("activeMenu", "po");
        model.addAttribute("suppliers", supplierService.getAllSupplier());
    }

    // list purchase order + search
    @GetMapping("")
    public String listPurchaseOrders(@ModelAttribute("criteria") PurchaseOrderSearchCriteria criteria, Model model) {
        List<PurchaseOrderResponse> purchaseOrders = orderService.searchPurchaseOrders(criteria);
        model.addAttribute("orders", purchaseOrders);
        return VIEW_ORDER_LIST;
    }

    // tạo form purchase order từ quotation
    @IsDirector
    @GetMapping("/create-from-quotation/{quotationId}")
    public String showCreateForm(@PathVariable("quotationId") Integer quotationId, Model model) {
        try {
            PurchaseOrderCreateRequest orderCreateRequest = orderService.preparePurchaseOrderForm(quotationId);
            model.addAttribute("orderCreateRequest", orderCreateRequest);
        } catch (InvalidDataException e) {
            model.addAttribute("error", e.getMessage());
        }

        return VIEW_ORDER_FORM;
    }

    // xử lý form
    @IsDirector
    @PostMapping("/create-from-quotation/{quotationId}")
    public String createOrder(@PathVariable("quotationId") Integer quotationId,
                              @ModelAttribute("orderCreateRequest") PurchaseOrderCreateRequest request,
                              BindingResult result,
                              @RequestParam(value = "removeLine", required = false) Integer removeLine,
                              Model model) {
        
        // logic xóa dòng
        if (removeLine != null && removeLine >= 0) {
            List<PurchaseOrderDetailCreateRequest> lines = request.getPurchaseOrderDetailCreateRequests();
            if (lines != null && lines.size() > 1 && removeLine < lines.size()) {

                // thực hiện xóa đi dòng cần xóa
                lines.remove(removeLine.intValue());

            }
            return VIEW_ORDER_FORM;
        }

        if (result.hasErrors()) {
            return VIEW_ORDER_FORM;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        try {

            Integer orderId = orderService.createPurchaseOrder(quotationId, request, username);

            return "redirect:/purchase-orders/" + orderId;
        } catch (InvalidDataException e) {
            model.addAttribute("error", e.getMessage());
            return VIEW_ORDER_FORM;
        }
    }

    // hiển thị po detail
    @GetMapping("/{id}")
    public String getOrderDetail(@PathVariable("id") Integer id, Model model) {
        PurchaseOrderResponse detail = orderService.getPurchaseOrderById(id);
        model.addAttribute("order", detail);
        return VIEW_ORDER_DETAIL;
    }

    // update ngày giao hàng
    @IsPurchaseStaff
    @PostMapping("/{id}/update-delivery-date")
    public String updateDeliveryDate(@PathVariable("id") Integer id,
                                     @RequestParam("deliveryDate") String deliveryDate,
                                     RedirectAttributes redirectAttributes) {
        try {
            orderService.updateDeliveryDate(id, deliveryDate);
            redirectAttributes.addFlashAttribute("message", "Cập nhật ngày giao hàng thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/purchase-orders/" + id;
    }

}
