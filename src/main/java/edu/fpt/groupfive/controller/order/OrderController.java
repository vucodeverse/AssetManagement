package edu.fpt.groupfive.controller.order;

import edu.fpt.groupfive.dto.request.OrderCreateRequest;
import edu.fpt.groupfive.dto.request.OrderSearchCriteria;
import edu.fpt.groupfive.dto.response.PurchaseOrderGroupResponse;
import edu.fpt.groupfive.dto.response.PurchaseOrderDetailResponse;
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
@Slf4j(topic = "ORDER-CONTROLLER")
public class OrderController {

    private static final int PAGE_SIZE = 4;

    private final OrderService orderService;
    private final SupplierService supplierService;
    private final OrderCalculationUtil orderCalculationUtil;

    @GetMapping("/purchase-orders")
    public String listPurchaseOrders(@ModelAttribute OrderSearchCriteria criteria,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model, jakarta.servlet.http.HttpServletRequest request) {
        log.info("List purchase orders, page={}", page);
        List<PurchaseOrderGroupResponse> all = orderService.getOrdersGroupedByPR(criteria);

        int totalGroups = all.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalGroups / PAGE_SIZE));
        page = Math.max(1, Math.min(page, totalPages));

        int fromIdx = (page - 1) * PAGE_SIZE;
        int toIdx = Math.min(fromIdx + PAGE_SIZE, totalGroups);
        List<PurchaseOrderGroupResponse> pageGroups = all.subList(fromIdx, toIdx);

        model.addAttribute("groups", pageGroups);
        model.addAttribute("criteria", criteria);
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("activeMenu", "po");
        return "order/order-from-purchase";
    }

    @GetMapping("/quotations/{quotationId}/create-po")
    public String createPurchseOrder(@PathVariable("quotationId") Integer quotationId, Model model,
            jakarta.servlet.http.HttpServletRequest request) {
        log.info("Load form purchase order");

        // load order create lên form
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest();
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

    @PostMapping(value = "/quotations/{quotationId}/create-po", params = "removeLine")
    public String removeOrderLine(@PathVariable("quotationId") Integer quotationId,
            @ModelAttribute("orderCreateRequest") OrderCreateRequest orderCreateRequest,
            @RequestParam("removeLine") int removeLine,
            Model model, jakarta.servlet.http.HttpServletRequest request) {
        log.info("Removing line {} from PO form", removeLine);

        var lines = orderCreateRequest.getOrderDetailCreateRequests();
        if (lines != null && removeLine >= 0 && removeLine < lines.size() && lines.size() > 1) {
            lines.remove(removeLine);
            orderCalculationUtil.recalculateTotal(orderCreateRequest);
        }

        model.addAttribute("orderCreateRequest", orderCreateRequest);
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("activeMenu", "po");
        return "order/order-form";
    }

    @PostMapping(value = "/quotations/{quotationId}/create-po", params = "!removeLine")
    public String createOrder(@PathVariable("quotationId") Integer quotationId,
            @ModelAttribute("orderCreateRequest") OrderCreateRequest orderCreateRequest,
            BindingResult result,
            Model model, jakarta.servlet.http.HttpServletRequest request) {
        log.info("Creating order for quotation {}", quotationId);

        model.addAttribute("suppliers", supplierService.getAllSupplier());
        model.addAttribute("activeMenu", "po");
        if (result.hasErrors()) {
            return "order/order-form";
        }

        // Recalculate to prevent tinkering with hidden field
        orderCalculationUtil.recalculateTotal(orderCreateRequest);

        try {
            Integer orderId = orderService.createOrder(quotationId, orderCreateRequest);
            String uri = request.getRequestURI().toLowerCase();
            String prefix = (uri.contains("purchase-staff") || uri.contains("purchase staff")
                    || uri.contains("purchase%20staff")) ? "/purchase-staff" : "/director";
            return "redirect:" + prefix + "/purchase-orders/" + orderId;
        } catch (edu.fpt.groupfive.util.exception.InvalidDataException e) {
            log.warn("Validation failed for order creation: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "order/order-form";
        }
    }

    @GetMapping("/purchase-orders/{id}")
    public String getOrderDetail(@PathVariable("id") Integer id, Model model,
            jakarta.servlet.http.HttpServletRequest request) {
        log.info("Get purchase order detail: {}", id);
        PurchaseOrderDetailResponse detail = orderService.getOrderDetail(id);
        model.addAttribute("order", detail);
        model.addAttribute("activeMenu", "po");
        return "order/order-of-purchase";
    }

}
