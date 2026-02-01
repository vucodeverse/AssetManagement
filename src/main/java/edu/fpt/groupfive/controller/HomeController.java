package edu.fpt.groupfive.controller;

import edu.fpt.groupfive.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "home";
    }
}
