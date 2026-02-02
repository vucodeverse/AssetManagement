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
        testDatabaseConnection();
    }

    private static void testDatabaseConnection() {
        DatabaseConfig databaseConfig = new DatabaseConfig();

        try (Connection conn = databaseConfig.getConnection()) {

            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ KẾT NỐI DATABASE THÀNH CÔNG!");
                System.out.println("➡️ URL đang kết nối: " + conn.getMetaData().getURL());
                System.out.println("➡️ Username: " + conn.getMetaData().getUserName());
            } else {
                System.out.println("❌ KẾT NỐI DATABASE THẤT BẠI!");
            }

        } catch (Exception e) {
            System.out.println("❌ LỖI KẾT NỐI DATABASE!");
            e.printStackTrace();
        }
    }


}