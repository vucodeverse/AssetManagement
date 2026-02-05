package edu.fpt.groupfive.controller.admin;

import edu.fpt.groupfive.dto.request.UseCreateRequest;
import edu.fpt.groupfive.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class UserController {
    private final UserService userService;

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("newUser", new UseCreateRequest());
        return "user-form";
    }

    @PostMapping("/save")
    public String addUser(@ModelAttribute("newUser") UseCreateRequest request) {
        userService.createUser(request);
        return "redirect:/admin/home";
    }

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/home";
    }
}
