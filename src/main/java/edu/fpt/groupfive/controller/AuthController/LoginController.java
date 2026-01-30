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

    private static final String MSG_LOGIN = "login";

    @GetMapping("/login")
    public String loginPage(){
        return MSG_LOGIN;
    }
}
