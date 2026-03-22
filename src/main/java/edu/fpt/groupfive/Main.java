package edu.fpt.groupfive;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class Main {
    public static void main(String[] args) throws Exception {

        // ===== Cấu hình DB (giống application.properties) =====
        String url = "jdbc:sqlserver://localhost:1433;databaseName=AssetManager;encrypt=true;trustServerCertificate=true";
        String dbUser = "sa";
        String dbPassword = "123";

        // ===== Mật khẩu mới muốn đặt =====
        String newPassword = "123"; // <-- Đổi mật khẩu ở đây

        // ===== Mã hóa bằng BCrypt (strength = 10, giống SecurityConfig) =====
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String hashedPassword = encoder.encode(newPassword);

        // ===== Kết nối DB và update =====
        Connection conn = DriverManager.getConnection(url, dbUser, dbPassword);

        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);

        // Update admin (userId = 1)
        ps.setString(1, hashedPassword);
        ps.setInt(2, 1);
        ps.executeUpdate();
        System.out.println("Updated admin (id=1) password");

        // Update user (userId = 2)
        // Tạo hash mới cho user (mỗi hash BCrypt là unique)
        ps.setString(1, encoder.encode(newPassword));
        ps.setInt(2, 2);
        ps.executeUpdate();
        System.out.println("Updated user (id=2) password");

        ps.close();
        conn.close();

        System.out.println("DONE! Mật khẩu mới: " + newPassword);
    }
}
