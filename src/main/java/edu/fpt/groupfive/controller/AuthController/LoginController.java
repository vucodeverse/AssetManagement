package edu.fpt.groupfive.controller.AuthController;

import edu.fpt.groupfive.dto.request.UserLoginRequest;
import edu.fpt.groupfive.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class LoginController {
    private final AuthService authService;
    private static final String MSG_MESSAGE = "errorMessage";
    private static final String MSG_LOGIN = "login";

    @GetMapping("/login")
    public String showLogin(){
        return MSG_LOGIN;
    }

    @PostMapping("/login")
    public String login(@ModelAttribute UserLoginRequest req, Model model){

        if(req.getUsername() == null || req.getUsername().isEmpty()){
            model.addAttribute(MSG_MESSAGE, "username must be not null");
            return MSG_LOGIN;
        }
        if(req.getPassword() == null || req.getPassword().isEmpty()){
            model.addAttribute(MSG_MESSAGE, "password must be not null");
            return MSG_LOGIN;
        }
        boolean check = authService.loginUser(req.getUsername(), req.getPassword());
        if(!check){
            model.addAttribute(MSG_MESSAGE, "Login failed");
            return MSG_LOGIN;
        }

        return "redirect:/home";
    }
}
