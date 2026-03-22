package edu.fpt.groupfive.controller.department;

import edu.fpt.groupfive.dto.response.DepartmentDashboardDTO;
import edu.fpt.groupfive.service.impl.DepartmentDashboardService;
import edu.fpt.groupfive.model.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("/department")
@RequiredArgsConstructor
public class DepartmentDashboardController {

    private final DepartmentDashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(@SessionAttribute("loggedInUser") Users currentUser, Model model) {

        DepartmentDashboardDTO dashboardData = dashboardService
                .getDashboardData(currentUser.getUserId());

        model.addAttribute("dashboardData", dashboardData);
        model.addAttribute("totalAssets", dashboardData.getTotalAssets());
        model.addAttribute("assetsInUse", dashboardData.getAssetsInUse());
        model.addAttribute("pendingAllocations", dashboardData.getPendingAllocations());
        model.addAttribute("totalAssetValue", dashboardData.getTotalAssetValue());
        model.addAttribute("assetDistribution", dashboardData.getAssetDistribution());
        model.addAttribute("recentRequests", dashboardData.getRecentRequests());
        model.addAttribute("departmentName", dashboardData.getDepartmentName());
        model.addAttribute("userName", dashboardData.getUserName());

        return "department/department-dashboard";
    }
}