package edu.fpt.groupfive.controller.manager;

import edu.fpt.groupfive.dto.response.AssetManagerDashboardResponse;
import edu.fpt.groupfive.service.AssetManagerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manager/dashboard")
@RequiredArgsConstructor
public class
AssetManagerDashboardController {

    private final AssetManagerDashboardService dashboardService;

    @GetMapping
    public String dashboard(Model model) {
        AssetManagerDashboardResponse dashboardData = dashboardService.getDashboardData();
        model.addAttribute("dashboard", dashboardData);
        model.addAttribute("activeMenu", "dashboard");
        return "manager/asset-manager-dashboard";
    }

}
