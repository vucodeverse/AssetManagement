package edu.fpt.groupfive.controller.department;

import edu.fpt.groupfive.dto.response.DepartmentDashboardDTO;
import edu.fpt.groupfive.service.UserService;
import edu.fpt.groupfive.service.impl.DepartmentDashboardService;
import edu.fpt.groupfive.model.Users;
import jakarta.servlet.http.HttpSession;
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
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        Users currentUser = userService.findById(userId);

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
        model.addAttribute("activeMenu", "dashboard");

        return "department/department-dashboard";
    }
}