package edu.fpt.groupfive;

import edu.fpt.groupfive.config.DatabaseConfig;
import edu.fpt.groupfive.dao.DepartmentDAO;
import edu.fpt.groupfive.dao.UserDAO;
import edu.fpt.groupfive.dao.impl.DepartmentDAOImpl;
import edu.fpt.groupfive.dao.impl.UserDAOImpl;
import edu.fpt.groupfive.dto.request.DepartmentCreateRequest;
import edu.fpt.groupfive.dto.request.UseCreateRequest;
import edu.fpt.groupfive.service.UserService;
import edu.fpt.groupfive.service.impl.DepartmentServiceImpl;
import edu.fpt.groupfive.service.impl.UserServiceImpl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class Main {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ KẾT NỐI DATABASE THÀNH CÔNG!");
            } else {
                System.out.println("❌ KẾT NỐI DATABASE THẤT BẠI!");
            }
        } catch (Exception e) {
            System.out.println("❌ LỖI KẾT NỐI DATABASE!");
            e.printStackTrace();
        }

//        DepartmentCreateRequest request = new DepartmentCreateRequest("Information Technology Department", null);
//
//        DepartmentDAO departmentDAO = new DepartmentDAOImpl();
//        DepartmentServiceImpl service = new DepartmentServiceImpl(departmentDAO);
//
//        service.createDepartment(request);

        UseCreateRequest request = new UseCreateRequest("admin", "123", "System",
                "system@gmail.com", "012345678", "ADMIN", 1);

        UserDAO userDAO = new UserDAOImpl();
        UserService userService = new UserServiceImpl(userDAO);

        userService.createUser(request);




    }
}