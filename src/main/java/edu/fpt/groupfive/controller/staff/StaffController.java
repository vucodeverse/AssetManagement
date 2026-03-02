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

    // hiển thị db của purcahse staff
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        StaffDashboardDTO dashboardData = dashboardService.getStaffDashboardData();
        model.addAttribute("dashboard", dashboardData);
        model.addAttribute("activeMenu", "dashboard");
        return "staff/staff-dashboard";
    }

    // lấy ra id cần truy cập vào detail
    @GetMapping("/purchases/{id}/purchase-detail")
    public String showPurchaseDetail(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("activeSub", "pr");
        model.addAttribute("activeMenu", "approval");
        model.addAttribute("purchase", purchaseService.findById(id));
        return "purchase/purchase-detail";
    }

    // duyệt purchase request
    @PostMapping("/purchases/{id}/actions")
    public String actionWithPr(@PathVariable("id") Integer id,
                               @RequestParam("actions") String actions,
            @RequestParam(value = "reasonReject", required = false) String reasonReject) {
        purchaseService.actionsWithPurchase(id, actions, reasonReject);
        return "redirect:/purchase-staff/purchases";
    }

    @ModelAttribute("searchAndFilter")
    public PurchaseRequestSearchCriteria initSearchAndFilter() {
        return new PurchaseRequestSearchCriteria();
    }
}
