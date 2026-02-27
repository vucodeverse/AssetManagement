package edu.fpt.groupfive.controller.director;

import edu.fpt.groupfive.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/director")
@RequiredArgsConstructor
@Slf4j(topic = "DIRECTOR-CONTROLLER")
public class DirectorController {
    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("dashboard", dashboardService.getDirectorDashboardData());
        return "director/director-dashboard";
    }
}
