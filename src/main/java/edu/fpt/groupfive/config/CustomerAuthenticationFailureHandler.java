package edu.fpt.groupfive.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomerAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        // lấy ra tk và mk
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        String msg = "";

        // check blank
        if(username == null || username.trim().isEmpty()  ){

        }else if(password == null || password.trim().isEmpty() ){

        }else{
            if(exception instanceof UsernameNotFoundException){

            }else if(exception instanceof BadCredentialsException){

            }else{

            }
        }

        request.getSession().setAttribute("error_login", msg);
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
