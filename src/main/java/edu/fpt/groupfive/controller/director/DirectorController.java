package edu.fpt.groupfive.controller.director;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.common.PurchaseProcessStatus;
import edu.fpt.groupfive.service.DashboardService;
import edu.fpt.groupfive.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/director")
@RequiredArgsConstructor
public class DirectorController {
    private final DashboardService dashboardService;
    private final PurchaseService purchaseService;

    // hiển thị dashboard
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("dashboard", dashboardService.getDirectorDashboardData());
        return "director/director-dashboard";
    }

    // hiển thị list purchase request để duyệt
    @GetMapping("/purchases")
    public String showPurchases(Model model) {
        model.addAttribute("activeMenu", "approval");
        model.addAttribute("activeSub", "pr");
        model.addAttribute("purchases", purchaseService.getPurchaseRequests());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("status", PurchaseProcessStatus.values());
        return "purchase/purchase-list";
    }

}
