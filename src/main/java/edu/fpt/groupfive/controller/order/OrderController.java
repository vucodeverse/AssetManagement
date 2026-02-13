package edu.fpt.groupfive.controller.order;

import edu.fpt.groupfive.dto.request.OrderCreateRequest;
import edu.fpt.groupfive.service.OrderService;
import edu.fpt.groupfive.util.exception.InvalidDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/director")
@RequiredArgsConstructor
@Slf4j(topic = "ORDER-CONTROLLER")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/quotations/{quotationId}/create-po")
    public String createPurchseOrder(@PathVariable("quotationId") Integer quotationId){
        log.info("Load form purchase order");

        // load order create lên form
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest();
        try {
            orderCreateRequest =
        }catch (InvalidDataException e){

        }
        return "";
    }
}
