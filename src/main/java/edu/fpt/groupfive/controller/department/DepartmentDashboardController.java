package edu.fpt.groupfive.controller.department;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/department")
public class DepartmentDashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("activeMenu", "dashboard");
        return "department/department-dashboard";
    }
}
