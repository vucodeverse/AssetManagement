package edu.fpt.groupfive.util.config.auth;

import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.model.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomerAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserDAO userDAO;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {


        // Lấy username từ authentication
        String username = authentication.getName();

        // Truy vấn DB lấy user info
        Users user = userDAO.findUserByUsername(username).orElse(null);

        // Lưu vào session
        HttpSession session = request.getSession();
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("departmentId", user.getDepartmentId());
        session.setAttribute("fullName", user.getFullName());
        session.setAttribute("role", user.getRole().name());


        String redirectUrl = request.getContextPath();

        // nếu role là director
        if (authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("DIRECTOR"))) {
            redirectUrl += "/director/dashboard";
        }

        if (authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("PURCHASE_STAFF"))) {
            redirectUrl += "/purchase-staff/dashboard";
        }

//        if (authentication.getAuthorities().stream().
//                anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ADMIN"))) {
//            if (authentication.getAuthorities().stream()
//                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ADMIN"))) {
//                redirectUrl += "/admin/home";
//            }

            if (authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("WAREHOUSE_STAFF"))) {
                redirectUrl += "/wh/dashboard";
            }


            if (authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ADMIN"))) {
                redirectUrl += "/admin/users";
            }

            if (authentication.getAuthorities().stream().
                    anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("DEPARTMENT_MANAGER"))) {
                redirectUrl += "/department/allocation-request/list";
            }
            if (authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ASSET_MANAGER"))) {
                redirectUrl += "/manager/dashboard";
            }

            if (authentication.getAuthorities().stream().
                    anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ASSET_MANAGER"))) {
                redirectUrl += "/asset-manager/allocation-request/list";
            }

            response.sendRedirect(redirectUrl);
//        }
    }
}
