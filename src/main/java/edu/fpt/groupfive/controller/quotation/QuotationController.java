package edu.fpt.groupfive.controller.quotation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/purchase-staff")
@RequiredArgsConstructor
public class QuotationController {

    @GetMapping("quotation")
    public String showForm(@ModelAttribute Model model){


        return "";
    }
}
