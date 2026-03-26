package edu.fpt.groupfive.util;

import edu.fpt.groupfive.common.Role;
import edu.fpt.groupfive.dto.request.DepartmentCreateRequest;
import edu.fpt.groupfive.dto.request.UserCreateRequest;
import edu.fpt.groupfive.dto.response.DepartmentResponse;
import edu.fpt.groupfive.dao.warehouse.WhWarehouseDAO;
import edu.fpt.groupfive.model.warehouse.Warehouse;
import edu.fpt.groupfive.service.DepartmentService;
import edu.fpt.groupfive.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final UserService userService;
    private final DepartmentService departmentService;
    private final WhWarehouseDAO whWarehouseDAO;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Ensure this only runs once for the root context
        if (event.getApplicationContext().getParent() == null) {
            initializeData();
        }
    }

    private void initializeData() {
        log.info("Starting data initialization...");
        initializeDepartmentsAndUsers();
        initializeWarehouse();
        log.info("Data initialization completed.");
    }

    private void initializeWarehouse() {
        if (!whWarehouseDAO.existsAny()) {
            log.info("No warehouses found. Creating default 'Main Warehouse'.");
            Warehouse w = new Warehouse();
            w.setName("Main Warehouse");
            w.setAddress("123 Logistics St.");
            w.setManagerUserId(1); // Will be updated if 1 doesn't exist, but usually 1 is the first user
            w.setStatus("ACTIVE");
            whWarehouseDAO.createWarehouse(w);
        }
    }

    private void initializeDepartmentsAndUsers() {
        List<DepartmentResponse> departments = departmentService.getAllDepartments();
        if (departments.isEmpty()) {
            log.info("No departments found. Creating default 'Administration' department.");
            DepartmentCreateRequest adminDeptRequest = new DepartmentCreateRequest();
            adminDeptRequest.setDepartmentName("Administration");
            adminDeptRequest.setDescription("Default administration department");
            departmentService.createDepartment(adminDeptRequest);
            // Refresh list
            departments = departmentService.getAllDepartments();
        }

        Integer defaultDeptId = departments.isEmpty() ? 1 : departments.get(0).getDepartmentId();

        // 2. Initialize accounts for all roles
        for (Role role : Role.values()) {
            String username = role.name().toLowerCase().replace("_", "");

            // Special case for existing admin
            if (role == Role.ADMIN) {
                username = "admin";
            }

            if (!userService.existsByUsername(username)) {
                log.info("Creating default account for role {}: {}...", role, username);

                UserCreateRequest request = new UserCreateRequest();
                request.setUsername(username);
                request.setPassword("123");
                request.setFirstName(role.getDisplayName());
                request.setLastName("User");
                request.setEmail(username + "@asset.com");
                request.setRole(role);
                request.setDepartmentId(defaultDeptId);

                try {
                    userService.createUser(request);
                    log.info("Created account: {} / {}123", username, username);
                } catch (Exception e) {
                    log.error("Failed to create account for role {}: {}", role, e.getMessage());
                }
            }
        }

        log.info("Data initialization completed.");
    }


}
