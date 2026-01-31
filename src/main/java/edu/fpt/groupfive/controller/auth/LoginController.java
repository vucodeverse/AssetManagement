package edu.fpt.groupfive.controller.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private static final String MSG_LOGIN = "login";

    @GetMapping("/login")
    public String loginPage(){
        return MSG_LOGIN;
    }
}
