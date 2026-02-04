package edu.fpt.groupfive.util.config.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class CustomerAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final MessageSource messageSource;


    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        // lấy ra tk và mk
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        String msg;

        // check blank
        if(username == null || username.trim().isEmpty()){
            msg = "login.error.empty.username";
        } else if(password == null || password.trim().isEmpty()){
            msg = "login.error.empty.password";
        } else if(exception instanceof UsernameNotFoundException){
            msg = "login.error.user.notfound";
        } else if(exception instanceof BadCredentialsException){
            msg = "login.error.bad.credentials";
        } else {
            msg = "login.error.default";
        }

        String errorMessage = messageSource.getMessage(
                msg,
                null,
                "Đăng nhập thất bại",
                Locale.getDefault()
        );
        request.getSession().setAttribute("username", username);
        request.getSession().setAttribute("error_login", errorMessage);
        response.sendRedirect(request.getContextPath() + "/auth/login");
    }


}
