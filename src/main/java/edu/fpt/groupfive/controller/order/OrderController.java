package edu.fpt.groupfive.controller.order;

import edu.fpt.groupfive.dto.request.PurchaseOrderCreateRequest;
import edu.fpt.groupfive.dto.request.PurchaseOrderSearchCriteria;
import edu.fpt.groupfive.dto.response.PurchaseOrderResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderFullResponse;
import edu.fpt.groupfive.service.OrderService;
import edu.fpt.groupfive.service.SupplierService;
import edu.fpt.groupfive.util.OrderCalculationUtil;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/purchase-staff/purchase-orders")
public class OrderController {

    private static final int PAGE_SIZE = 4;

    private final OrderService orderService;
    private final SupplierService supplierService;
    private final OrderCalculationUtil orderCalculationUtil;

    // list purchase order
    @GetMapping("")
    public String listPurchaseOrders(@ModelAttribute PurchaseOrderSearchCriteria criteria, Model model) {
        List<PurchaseOrderResponse> purchaseOrders = orderService.getPurchaseOrdersFlat(criteria);

        model.addAttribute("orders", purchaseOrders);
        model.addAttribute("criteria", criteria);
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("activeMenu", "po");
        return "order/order-from-purchase";
    }

    // tạo purchase order từ quotaiton
    @GetMapping("/create-from-quotation/{quotationId}")
    public String createPurchseOrder(@PathVariable("quotationId") Integer quotationId, Model model) {

        // load order create lên form
        PurchaseOrderCreateRequest orderCreateRequest;
        try {
            orderCreateRequest = orderService.checkFormCreateOrder(quotationId);
        } catch (InvalidDataException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("activeMenu", "po");
            return "order/order-form";
        }

        model.addAttribute("orderCreateRequest", orderCreateRequest);
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("activeMenu", "po");
        return "order/order-form";
    }

    // xóa 1 dòng
    @PostMapping(value = "/create-from-quotation/{quotationId}", params = "removeLine")
    public String removeOrderLine(@PathVariable("quotationId") Integer quotationId,
            @ModelAttribute("orderCreateRequest") PurchaseOrderCreateRequest orderCreateRequest,
            @RequestParam("removeLine") int removeLine,
            Model model, jakarta.servlet.http.HttpServletRequest request) {

        var lines = orderCreateRequest.getPurchaseOrderDetailCreateRequests();
        if (lines != null && removeLine >= 0 && removeLine < lines.size() && lines.size() > 1) {
            lines.remove(removeLine);
            orderCalculationUtil.recalculateTotal(orderCreateRequest);
        }

        model.addAttribute("orderCreateRequest", orderCreateRequest);
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("activeMenu", "po");
        return "order/order-form";
    }

    // create po
    @PostMapping(value = "/create-from-quotation/{quotationId}", params = "!removeLine")
    public String createOrder(@PathVariable("quotationId") Integer quotationId,
            @ModelAttribute("orderCreateRequest") PurchaseOrderCreateRequest orderCreateRequest,
            BindingResult result,
            Model model, jakarta.servlet.http.HttpServletRequest request) {

        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("activeMenu", "po");
        if (result.hasErrors()) {
            return "order/order-form";
        }

        orderCalculationUtil.recalculateTotal(orderCreateRequest);

        try {
            Integer orderId = orderService.createOrder(quotationId, orderCreateRequest);
            return "redirect:/purchase-staff/purchase-orders/" + orderId;
        } catch (edu.fpt.groupfive.util.exception.InvalidDataException e) {
            model.addAttribute("error", e.getMessage());
            return "order/order-form";
        }
    }

    // hiển thị po detail
    @GetMapping("/{id}")
    public String getOrderDetail(@PathVariable("id") Integer id, Model model,
            jakarta.servlet.http.HttpServletRequest request) {
        PurchaseOrderFullResponse detail = orderService.getOrderDetail(id);
        model.addAttribute("order", detail);
        model.addAttribute("activeMenu", "po");
        return "order/order-of-purchase";
    }

}
