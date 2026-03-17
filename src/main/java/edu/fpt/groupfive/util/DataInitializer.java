package edu.fpt.groupfive.util;

import edu.fpt.groupfive.common.Role;
import edu.fpt.groupfive.dao.DepartmentDAO;
import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.model.Department;
import edu.fpt.groupfive.model.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final UserDAO userDAO;
    private final DepartmentDAO departmentDAO;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Ensure this only runs once for the root context
        if (event.getApplicationContext().getParent() == null) {
            initializeData();
        }
    }

    private void initializeData() {
        log.info("Checking for default admin account...");
        
        // 1. Ensure at least one department exists
        List<Department> departments = departmentDAO.findAll();
        if (departments.isEmpty()) {
            log.info("No departments found. Creating default 'Administration' department.");
            Department adminDept = new Department();
            adminDept.setDepartmentName("Administration");
            adminDept.setStatus("ACTIVE");
            adminDept.setCreatedDate(LocalDateTime.now());
            departmentDAO.insert(adminDept);
            // Refresh list
            departments = departmentDAO.findAll();
        }

        // 2. Ensure admin user exists
        if (!userDAO.existsByUsername("admin")) {
            log.info("Admin account not found. Creating default admin...");
            
            Integer deptId = departments.isEmpty() ? 1 : departments.get(0).getDepartmentId();

            Users admin = new Users();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setEmail("admin@asset.com");
            admin.setRole(Role.ADMIN);
            admin.setStatus("ACTIVE");
            admin.setDepartmentId(deptId);
            admin.setCreatedDate(LocalDateTime.now());
            
            userDAO.insert(admin);
            log.info("Default admin account created: admin / admin123");
        } else {
            log.info("Admin account already exists.");
        }
    }
}
