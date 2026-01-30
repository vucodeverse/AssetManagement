package edu.fpt.groupfive.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

public class DatabaseConfig {

    private static String url;
    private static String username;
    private static String password;

    static {
        try {
            Map<String, Object> yaml = YamlConfigLoader.load();
            Map<String, Object> spring = (Map<String, Object>) yaml.get("spring");
            Map<String, Object> datasource = (Map<String, Object>) spring.get("datasource");

            url = datasource.get("url").toString();
            username = datasource.get("username").toString();
            password = datasource.get("password").toString();

            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

        } catch (Exception e) {
            throw new RuntimeException("Failed to load DB config from YAML", e);
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            throw new RuntimeException("Cannot connect to database", e);
        }
    }
}
