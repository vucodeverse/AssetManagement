package edu.fpt.groupfive.controller.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/director")
@RequiredArgsConstructor
public class OrderController {

    @PostMapping("/quotations/{quotationId}/create-po")
    public String createPurchseOrder(@PathVariable("quotationId") Integer quotationId){

        return "";
    }
}
