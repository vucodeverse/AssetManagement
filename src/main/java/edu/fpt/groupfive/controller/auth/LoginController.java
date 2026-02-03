package edu.fpt.groupfive.controller.auth;

import edu.fpt.groupfive.dto.request.UserLoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller()
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class LoginController {


    @GetMapping("/login")
    public String loginProcess(@ModelAttribute("loginRequest") UserLoginRequest userLoginReqest, Model model,
    HttpServletRequest request){
        log.info("Post login controller");

            Object errLogin = request.getSession().getAttribute("error_login");
        if(errLogin != null){
            model.addAttribute("errorLogin", errLogin.toString());
            userLoginReqest.setUsername((String) request.getSession().getAttribute("username"));
            userLoginReqest.setPassword((String) request.getSession().getAttribute("password"));

            // xóa session sau khi đã gửi lỗi
            request.getSession().removeAttribute("error_login");
            request.getSession().removeAttribute("username");
            request.getSession().removeAttribute("password");
        }
        return "auth/login";
    }

}
