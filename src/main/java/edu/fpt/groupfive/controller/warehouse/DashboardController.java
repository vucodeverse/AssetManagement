package edu.fpt.groupfive.controller.warehouse;

import edu.fpt.groupfive.dto.warehouse.response.DashboardMetricsDto;
import edu.fpt.groupfive.service.warehouse.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/wh/{userId}")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public String dashboard(@PathVariable("userId") Integer userId, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            DashboardMetricsDto metrics = dashboardService.getDashboardData(userId);
            model.addAttribute("metrics", metrics);
            model.addAttribute("userId", userId);
            model.addAttribute("activeMenu", "dashboard");
            return "warehouse/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // If the user doesn't have a warehouse, they shouldn't be here, redirect to
            // home or login
            return "redirect:/";
        }
    }
}
