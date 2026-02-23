package e2e.utils;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;

public class ExcelChecker {
    @Test
    void checkRows() throws IOException {
        String path = "src/test/resources/PurchaseFormTestData.xlsx";
        List<Object[]> data = ExcelUtils.readExcelData(path, "TestCases");
        System.out.println("TOTAL_ROWS_FOUND: " + data.size());
        for (Object[] row : data) {
            System.out.println("ROW_ID: " + row[0] + " | RESULT: " + (row.length > 23 ? row[23] : "N/A"));
        }
    }
}
