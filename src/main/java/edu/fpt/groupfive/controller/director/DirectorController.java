package edu.fpt.groupfive.controller.director;

import edu.fpt.groupfive.common.Priority;
import edu.fpt.groupfive.common.Status;
import edu.fpt.groupfive.common.Request;
import edu.fpt.groupfive.dto.request.PurchaseRequestSearchCriteria;
import edu.fpt.groupfive.dto.request.QuotationSearchCriteria;
import edu.fpt.groupfive.service.DashboardService;
import edu.fpt.groupfive.service.PurchaseService;
import edu.fpt.groupfive.service.QuotationService;
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
    private final QuotationService quotationService;

    @ModelAttribute("searchAndFilter")
    public PurchaseRequestSearchCriteria initSearchAndFilter() {
        return new PurchaseRequestSearchCriteria();
    }

    @ModelAttribute("searchForQuotation")
    public QuotationSearchCriteria initSearchForQuotation() {
        return new QuotationSearchCriteria();
    }

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
        model.addAttribute("status", Request.values());
        return "purchase/purchase-list";
    }

    // hiển thị list quotation để duyệt
    @GetMapping("/quotations")
    public String showQuotations(Model model) {
        model.addAttribute("activeMenu", "approval");
        model.addAttribute("activeSub", "qt");
        model.addAttribute("quotations", quotationService.getQuotationAndPurchase());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("status", Status.values());
        return "quotation/quotation-list";
    }
}
