package edu.fpt.groupfive;

import edu.fpt.groupfive.config.database.DatabaseConfig;

import java.sql.Connection;

public class Main {
    public static void main(String[] args) {

        DatabaseConfig databaseConfig = new DatabaseConfig();

        try (Connection conn = databaseConfig.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ KẾT NỐI DATABASE THÀNH CÔNG!");
            } else {
                System.out.println("❌ KẾT NỐI DATABASE THẤT BẠI!");
            }
        } catch (Exception e) {
            System.out.println("❌ LỖI KẾT NỐI DATABASE!");
            e.printStackTrace();
        }

//        DepartmentCreateRequest request1 = new DepartmentCreateRequest("Information Technology Department", null);
//        DepartmentCreateRequest request2 = new DepartmentCreateRequest("Asset Management Department", null);
//
//        DepartmentDAO departmentDAO = new DepartmentDAOImpl();
//        DepartmentServiceImpl service = new DepartmentServiceImpl(departmentDAO);
//
//        service.createDepartment(request1);
//        service.createDepartment(request2);
//
//        UseCreateRequest requestu1 = new UseCreateRequest("admin", "123", "System",
//                "system@gmail.com", "012345678", "ADMIN", 1);
//        UseCreateRequest requestu2 = new UseCreateRequest("manager1", "123", "Manager",
//                "manage1@gmail.com", "012345688", "MANAGER", 2);
//
//        UserDAO userDAO = new UserDAOImpl();
//        UserService userService = new UserServiceImpl(userDAO);
//
//        userService.createUser(requestu1);
//        userService.createUser(requestu2);




    }
}