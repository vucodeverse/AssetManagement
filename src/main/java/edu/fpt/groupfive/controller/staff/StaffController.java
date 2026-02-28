package edu.fpt.groupfive.controller.staff;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dto.request.PurchaseSearchAndFilter;
import edu.fpt.groupfive.dto.response.StaffDashboardDTO;
import edu.fpt.groupfive.service.AssetTypeService;
import edu.fpt.groupfive.service.DashboardService;
import edu.fpt.groupfive.service.PurchaseService;
import edu.fpt.groupfive.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class StaffController {
    private final PurchaseService purchaseService;
    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        StaffDashboardDTO dashboardData = dashboardService.getStaffDashboardData();
        model.addAttribute("dashboard", dashboardData);
        model.addAttribute("activeMenu", "dashboard");
        return "staff/staff-dashboard";
    }

    // hiển thị màn purchase request list
    @GetMapping("/purchases")
    public String showPurchases(Model model) {
        model.addAttribute("activeSub", "pr");
        model.addAttribute("activeMenu", "approval");
        model.addAttribute("purchases", purchaseService.findAllPurchases());
        addStaPri(model);
        return "purchase/purchase-list";
    }

    private static void addStaPri(Model model) {
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("status", Request.values());
    }

    // search and filter
    @GetMapping("/purchase/search-filter")
    public String searchAndfilter(@ModelAttribute("searchAndFilter") PurchaseSearchAndFilter purchaseSearchAndFilter,
            Model model) {
        model.addAttribute("activeSub", "pr");
        model.addAttribute("activeMenu", "approval");
        model.addAttribute("purchases", purchaseService.searchAndFilter(purchaseSearchAndFilter));
        addStaPri(model);
        return "purchase/purchase-list";
    }

    // lấy ra id cần truy cập vào detail
    @GetMapping("/purchases/{id}/purchase-detail")
    public String showPurchaseDetail(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("activeSub", "pr");
        model.addAttribute("activeMenu", "approval");
        model.addAttribute("purchase", purchaseService.findById(id));
        return "purchase/purchase-detail";
    }

    @PostMapping("/purchases/{id}/actions")
    public String actionWithPr(@PathVariable("id") Integer id, @RequestParam("actions") String actions,
            @RequestParam(value = "reasonReject", required = false) String reasonReject) {
        purchaseService.actionsWithPurchase(id, actions, reasonReject);
        return "redirect:/purchases";
    }

    @ModelAttribute("searchAndFilter")
    public PurchaseSearchAndFilter initSearchAndFilter() {
        return new PurchaseSearchAndFilter();
    }
}
