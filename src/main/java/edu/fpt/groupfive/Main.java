package edu.fpt.groupfive;

import edu.fpt.groupfive.config.DatabaseConfig;

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
    }
}