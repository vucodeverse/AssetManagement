package edu.fpt.groupfive;

import edu.fpt.groupfive.config.DatabaseConfig;
import edu.fpt.groupfive.dao.DepartmentDAO;
import edu.fpt.groupfive.dao.impl.DepartmentDAOImpl;
import edu.fpt.groupfive.dto.request.DepartmentCreateRequest;
import edu.fpt.groupfive.service.impl.DepartmentServiceImpl;

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

        DepartmentCreateRequest request = new DepartmentCreateRequest("Information Technology Department", null);

        DepartmentDAO departmentDAO = new DepartmentDAOImpl();
        DepartmentServiceImpl service = new DepartmentServiceImpl(departmentDAO);

        service.createDepartment(request);


    }
}