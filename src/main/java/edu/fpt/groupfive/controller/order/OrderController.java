package edu.fpt.groupfive.controller.order;

import edu.fpt.groupfive.dto.request.OrderCreateRequest;
import edu.fpt.groupfive.dto.request.OrderSearchCriteria;
import edu.fpt.groupfive.dto.response.PurchaseOrderGroupResponse;
import edu.fpt.groupfive.service.OrderService;
import edu.fpt.groupfive.service.SupplierService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/director")
@RequiredArgsConstructor
@Slf4j(topic = "ORDER-CONTROLLER")
public class OrderController {

    private static final int PAGE_SIZE = 4;

    private final OrderService orderService;
    private final SupplierService supplierService;

    // ── Purchase Order List ────────────────────────────────────
    @GetMapping("/purchase-orders")
    public String listPurchaseOrders(@ModelAttribute OrderSearchCriteria criteria,
                                     @RequestParam(defaultValue = "1") int page,
                                     Model model) {
        log.info("List purchase orders, page={}", page);
        List<PurchaseOrderGroupResponse> all = orderService.getOrdersGroupedByPR(criteria);

        int totalGroups = all.size();
        int totalPages  = Math.max(1, (int) Math.ceil((double) totalGroups / PAGE_SIZE));
        page = Math.max(1, Math.min(page, totalPages));

        int fromIdx = (page - 1) * PAGE_SIZE;
        int toIdx   = Math.min(fromIdx + PAGE_SIZE, totalGroups);
        List<PurchaseOrderGroupResponse> pageGroups = all.subList(fromIdx, toIdx);

        model.addAttribute("groups",     pageGroups);
        model.addAttribute("criteria",   criteria);
        model.addAttribute("suppliers",  supplierService.getAllSupplier());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages",  totalPages);
        model.addAttribute("activeMenu", "orders");
        return "order/order-from-purchase";
    }

    @GetMapping("/quotations/{quotationId}/create-po")
    public String createPurchseOrder(@PathVariable("quotationId") Integer quotationId, Model model) {
        log.info("Load form purchase order");

        // load order create lên form
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest();
        try {
            orderCreateRequest = orderService.checkFormCreateOrder(quotationId);
        }catch (InvalidDataException e){
            model.addAttribute("error", e.getMessage());
            return "order/order-form";
        }

        model.addAttribute("orderCreateRequest", orderCreateRequest);
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        return "order/order-form";
    }

    @PostMapping("/quotations/{quotationId}/create-po")
    public String createOrder(@PathVariable("quotationId") Integer quotationId, @ModelAttribute("orderCreateRequest") OrderCreateRequest orderCreateRequest,
                              BindingResult result,
                              Model model) {
        log.info("Create order");

        model.addAttribute("suppliers", supplierService.getAllSupplier());
        if(result.hasErrors()){
            return "order/order-form";
        }

        orderService.createOrder(quotationId, orderCreateRequest);
        return "redirect:/director/quotations";
    }


    /**
     * Xóa 1 dòng order detail.
     * Reload data từ DB, remove theo index, render lại form.
     */
    @PostMapping("/quotations/{quotationId}/create-po/remove-line")
    public String removeOrderLine(@PathVariable("quotationId") Integer quotationId,
                                  @RequestParam("deleteIndex") int deleteIndex,
                                  Model model) {
        log.info("Remove order detail line {} from quotation {}", deleteIndex, quotationId);

        OrderCreateRequest orderCreateRequest;
        try {
            orderCreateRequest = orderService.checkFormCreateOrder(quotationId);
        } catch (InvalidDataException e) {
            model.addAttribute("error", e.getMessage());
            return "order/order-form";
        }

        var lines = orderCreateRequest.getOrderDetailCreateRequests();
        if (lines != null && deleteIndex >= 0 && deleteIndex < lines.size() && lines.size() > 1) {
            lines.remove(deleteIndex);
        }

        model.addAttribute("orderCreateRequest", orderCreateRequest);
        model.addAttribute("suppliers", supplierService.getAllSupplier());
        return "order/order-form";
    }

}
