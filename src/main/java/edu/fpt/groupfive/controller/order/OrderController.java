package edu.fpt.groupfive.controller.order;

import edu.fpt.groupfive.dto.request.OrderCreateRequest;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.OrderService;
import edu.fpt.groupfive.service.SupplierService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/director")
@RequiredArgsConstructor
@Slf4j(topic = "ORDER-CONTROLLER")
public class OrderController {

    private final OrderService orderService;
    private final SupplierService supplierService;

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


}
