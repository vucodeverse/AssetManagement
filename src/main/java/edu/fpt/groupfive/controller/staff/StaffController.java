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
@RequestMapping("/purchase-staff")
@RequiredArgsConstructor
@Slf4j(topic = "STAFF-CONTROLLER")
public class StaffController {
    private final PurchaseService purchaseService;
    private final AssetTypeService assetTypeService;
    private final UserService userService;
    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, jakarta.servlet.http.HttpServletRequest request) {
        StaffDashboardDTO dashboardData = dashboardService.getStaffDashboardData();
        model.addAttribute("dashboard", dashboardData);
        model.addAttribute("activeMenu", "dashboard");

        String uri = request.getRequestURI().toLowerCase();
        String prefix = (uri.contains("purchase-staff") || uri.contains("purchase staff")
                || uri.contains("purchase%20staff")) ? "/purchase-staff" : "/director";
        model.addAttribute("linkPrefix", prefix);
        model.addAttribute("layoutName", "layout/staff-layout");

        return "staff/staff-dashboard";
    }

    // hiển thị màn purchase request list
    @GetMapping("/purchases")
    public String showPurchases(Model model, jakarta.servlet.http.HttpServletRequest request) {
        model.addAttribute("activeSub", "pr");
        model.addAttribute("activeMenu", "approval");
        model.addAttribute("purchases", purchaseService.findAllPurchases());
        addStaPri(model);
        addLayout(model, request);
        return "purchase/purchase-list";
    }

    private static void addStaPri(Model model) {
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("status", Request.values());
    }

    // search and filter
    @GetMapping("/purchase/search-filter")
    public String searchAndfilter(@ModelAttribute("searchAndFilter") PurchaseSearchAndFilter purchaseSearchAndFilter,
            Model model, jakarta.servlet.http.HttpServletRequest request) {
        model.addAttribute("activeSub", "pr");
        model.addAttribute("activeMenu", "approval");
        model.addAttribute("purchases", purchaseService.searchAndFilter(purchaseSearchAndFilter));
        addStaPri(model);
        addLayout(model, request);
        return "purchase/purchase-list";
    }

    // lấy ra id cần truy cập vào detail
    @GetMapping("/purchases/{id}/purchase-detail")
    public String showPurchaseDetail(@PathVariable("id") Integer id, Model model,
            jakarta.servlet.http.HttpServletRequest request) {
        model.addAttribute("activeSub", "pr");
        model.addAttribute("activeMenu", "approval");
        model.addAttribute("purchase", purchaseService.findById(id));
        addLayout(model, request);
        return "purchase/purchase-detail";
    }

    @PostMapping("/purchases/{id}/actions")
    public String actionWithPr(@PathVariable("id") Integer id, Model model, @RequestParam("actions") String actions,
            @RequestParam(value = "reasonReject", required = false) String reasonReject,
            jakarta.servlet.http.HttpServletRequest request) {
        purchaseService.actionsWithPurchase(id, actions, reasonReject);
        String uri = request.getRequestURI().toLowerCase();
        String prefix = (uri.contains("purchase-staff") || uri.contains("purchase staff")
                || uri.contains("purchase%20staff")) ? "/purchase-staff" : "/director";
        return "redirect:" + prefix + "/purchases";
    }

    private void addLayout(Model model, jakarta.servlet.http.HttpServletRequest request) {
        String uri = request.getRequestURI().toLowerCase();
        String prefix = (uri.contains("purchase-staff") || uri.contains("purchase staff")
                || uri.contains("purchase%20staff")) ? "/purchase-staff" : "/director";
        model.addAttribute("linkPrefix", prefix);

        if (uri.contains("purchase-staff") || uri.contains("purchase staff") || uri.contains("purchase%20staff")) {
            model.addAttribute("layoutName", "layout/staff-layout");
        } else {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_PURCHASE_STAFF") ||
                            a.getAuthority().equals("PURCHASE_STAFF"))) {
                model.addAttribute("layoutName", "layout/staff-layout");
            } else {
                model.addAttribute("layoutName", "layout/director-layout");
            }
        }
    }

    @ModelAttribute("searchAndFilter")
    public PurchaseSearchAndFilter initSearchAndFilter() {
        return new PurchaseSearchAndFilter();
    }
}
