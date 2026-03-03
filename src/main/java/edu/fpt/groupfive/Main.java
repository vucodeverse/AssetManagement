package edu.fpt.groupfive;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Main {
    public static void main(String[] args) {
//            BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
//            System.out.println(enc.encode("123456"));
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication.getName());
    }

//    private static void testDatabaseConnection() {
//        DatabaseConfig databaseConfig = new DatabaseConfig();
//
//        try (Connection conn = databaseConfig.getConnection()) {
//
//            if (conn != null && !conn.isClosed()) {
//                System.out.println("✅ KẾT NỐI DATABASE THÀNH CÔNG!");
//                System.out.println("➡️ URL đang kết nối: " + conn.getMetaData().getURL());
//                System.out.println("➡️ Username: " + conn.getMetaData().getUserName());
//            } else {
//                System.out.println("❌ KẾT NỐI DATABASE THẤT BẠI!");
//            }
//
//        } catch (Exception e) {
//            System.out.println("❌ LỖI KẾT NỐI DATABASE!");
//            e.printStackTrace();
//        }
//    }


}