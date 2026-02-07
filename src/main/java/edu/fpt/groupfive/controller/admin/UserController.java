package edu.fpt.groupfive.controller.admin;

import edu.fpt.groupfive.common.Role;
import edu.fpt.groupfive.dto.request.UserCreateRequest;
import edu.fpt.groupfive.dto.request.UserUpdateRequest;
import edu.fpt.groupfive.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class UserController {

    private final UserService userService;

    @GetMapping("/home")
    public String homePage(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "home";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("user", new UserCreateRequest());
        model.addAttribute("roles", Role.values());
        model.addAttribute("mode", "Add");
        return "user-detail";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("roles", Role.values());
        model.addAttribute("mode", "Edit");
        return "user-detail";
    }

    @PostMapping("/create")
    public String createUser(
            @Valid @ModelAttribute("user") UserCreateRequest request,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("mode", "Add");
            return "user-detail";
        }

        userService.createUser(request);
        return "redirect:/admin/home";
    }

    @PostMapping("/update")
    public String updateUser(
            @Valid @ModelAttribute("user") UserUpdateRequest request,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("mode", "Edit");
            return "user-detail";
        }

        userService.updateUser(request);
        return "redirect:/admin/home";
    }


    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Integer userId) {
        userService.removeUser(userId);
        return "redirect:/admin/home";
    }


}
