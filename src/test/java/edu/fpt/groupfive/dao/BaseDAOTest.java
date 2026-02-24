package edu.fpt.groupfive.dao;

import edu.fpt.groupfive.util.config.database.DatabaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Connection;

@ExtendWith(SpringExtension.class)
// Chỉ nạp DatabaseConfig chung cho toàn bộ các test
@ContextConfiguration(classes = {DatabaseConfig.class})
// Trỏ tới cấu hình H2
@TestPropertySource(locations = "classpath:application-test.properties")
public abstract class BaseDAOTest {

    @Autowired
    protected DatabaseConfig databaseConfig; // Để protected phòng khi class con cần dùng Connection

    // Hàm này sẽ tự động chạy trước MỖI @Test của các class con
    @BeforeEach
    void setUpDatabase() {
        try (Connection conn = databaseConfig.getConnection()) {
            // Xóa và tạo lại bảng trước mỗi test case để dữ liệu luôn sạch
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("schema.sql"));
        } catch (Exception e) {
            throw new RuntimeException("Không thể khởi tạo schema cho H2", e);
        }
    }
}