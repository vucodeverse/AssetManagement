package edu.fpt.groupfive;

public class Main {
    public static void main(String[] args) {
        //testDatabaseConnection();
        System.out.println("Hello world");
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