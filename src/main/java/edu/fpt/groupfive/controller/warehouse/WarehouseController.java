package edu.fpt.groupfive.controller.warehouse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/whs")
public class WarehouseController {

    @GetMapping(path = "/dashboard")
    public String showDashboard(){
        return "page/warehouse/dashboard";
    }

}
