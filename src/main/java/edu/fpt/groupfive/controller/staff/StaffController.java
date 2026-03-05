package edu.fpt.groupfive.controller.staff;

import edu.fpt.groupfive.dto.request.PurchaseRequestSearchCriteria;
import edu.fpt.groupfive.dto.response.StaffDashboardDTO;
import edu.fpt.groupfive.service.DashboardService;
import edu.fpt.groupfive.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/purchase-staff")
public class StaffController {
    private final PurchaseService purchaseService;
    private final DashboardService dashboardService;

    // hiển thị dashboard của purchase staff
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        StaffDashboardDTO dashboardData = dashboardService.getStaffDashboardData();
        model.addAttribute("dashboard", dashboardData);
        model.addAttribute("activeMenu", "dashboard");
        return "staff/staff-dashboard";
    }

    @ModelAttribute("searchAndFilter")
    public PurchaseRequestSearchCriteria initSearchAndFilter() {
        return new PurchaseRequestSearchCriteria();
    }
}
