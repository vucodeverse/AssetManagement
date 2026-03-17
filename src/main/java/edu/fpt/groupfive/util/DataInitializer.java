package edu.fpt.groupfive.util;

import edu.fpt.groupfive.common.Role;
import edu.fpt.groupfive.dto.request.DepartmentCreateRequest;
import edu.fpt.groupfive.dto.request.UserCreateRequest;
import edu.fpt.groupfive.dto.response.DepartmentResponse;
import edu.fpt.groupfive.service.DepartmentService;
import edu.fpt.groupfive.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final UserService userService;
    private final DepartmentService departmentService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Ensure this only runs once for the root context
        if (event.getApplicationContext().getParent() == null) {
            initializeData();
            initializeWarehouseData();
        }
    }

    private void initializeData() {
        log.info("Starting data initialization...");
        
        // 1. Ensure at least one department exists
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
                request.setPassword(username + "123");
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

    private void initializeWarehouseData() {
        log.info("Initializing warehouse infrastructure...");

        // 1. Ensure a default warehouse exists
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wh_warehouses", Integer.class);
        if (count == null || count == 0) {
            log.info("No warehouses found. Creating default 'Procurex Central Warehouse'.");
            // Find an admin user to be the manager
            List<Integer> adminIds = jdbcTemplate.queryForList(
                "SELECT user_id FROM users WHERE role = 'ADMIN' ORDER BY user_id", Integer.class);

            if (!adminIds.isEmpty()) {
                jdbcTemplate.update(
                    "INSERT INTO wh_warehouses (name, address, manager_user_id, status) VALUES (?, ?, ?, ?)",
                    "Procurex Central Warehouse", "123 Business Way, Tech Park", adminIds.get(0), "ACTIVE"
                );
                log.info("Default warehouse created.");
            } else {
                log.warn("Cannot create warehouse: No admin user found.");
            }
        }

        // 2. Ensure some default asset capacities exist (optional but helpful)
        Integer capacityCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wh_asset_capacity", Integer.class);
        if (capacityCount == null || capacityCount == 0) {
            log.info("Adding default asset type capacities...");
            jdbcTemplate.execute("""
                INSERT INTO wh_asset_capacity (asset_type_id, unit_volume)
                SELECT asset_type_id, 1 FROM asset_type
                WHERE asset_type_id NOT IN (SELECT asset_type_id FROM wh_asset_capacity)
            """);
        }
    }
}
