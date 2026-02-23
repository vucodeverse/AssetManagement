package e2e.utils;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelRestorer {

    @Test
    void restoreData() throws IOException {
        String filePath = "src/test/resources/PurchaseFormTestData.xlsx";
        List<Object[]> data = new ArrayList<>();
        
        // Header
        data.add(new Object[]{
            "testId", "action", "reason", "neededByDate", "priority", "note",
            "i1_type", "i1_qty", "i1_price", "i1_spec", "i1_note",
            "i2_type", "i2_qty", "i2_price", "i2_spec", "i2_note",
            "i3_type", "i3_qty", "i3_price", "i3_spec", "i3_note",
            "expectedOutcome", "expectedField", "expectedValue"
        });
        
        // 1. Needed date in past
        data.add(new Object[]{
            "PR_04_date_past", "SUBMIT", "Buy laptop", "2020-01-01", "High", "Automation note",
            "Laptop", "1", "10000000", "Spec", "Note",
            "", "", "", "", "",
            "", "", "", "", "",
            "ERROR", "neededByDate", "Thời gian nhập vào phải lớn hơn thời gian hiện tại"
        });
        
        // 2. Remove row test
        data.add(new Object[]{
            "PR_13_remove_row", "REMOVE_ROW", "", "", "", "",
            "", "", "", "", "",
            "", "", "", "", "",
            "", "", "", "", "",
            "UI_LOGIC", "", "-1"
        });

        // 3. Multi-item success
        data.add(new Object[]{
            "PR_14_multi_item", "SUBMIT", "Multi items", "2026-02-28", "High", "Automation note",
            "Laptop", "2", "15000000", "Laptop spec", "Laptop note",
            "Mouse", "5", "200000", "Mouse spec", "Mouse note",
            "Monitor", "2", "3500000", "Monitor spec", "Monitor note",
            "REDIRECT", "", "/purchase"
        });

        // 4. Save as Draft
        data.add(new Object[]{
            "PR_15_draft", "DRAFT", "Draft PR", null, "Medium", "Will complete later",
            "", "", "", "", "",
            "", "", "", "", "",
            "", "", "", "", "",
            "REDIRECT", "", "/purchase"
        });

        // 5. Bug actions missing (JS Submit)
        data.add(new Object[]{
            "BUG_actions_missing", "SUBMIT_JS", "Buy laptop", "2026-02-28", "High", "Automation note",
            "Laptop", "1", "10000000", "Spec", "Note",
            "", "", "", "", "",
            "", "", "", "", "",
            "SUCCESS_NO_400", "", ""
        });

        // 6. Invalid priority
        data.add(new Object[]{
            "TC_priority_invalid", "SUBMIT", "Buy laptop", "2026-02-28", "null", "Automation note",
            "Laptop", "1", "10000000", "Spec", "Note",
            "", "", "", "", "",
            "", "", "", "", "",
            "ERROR", "priority", ""
        });

        // 7. Multi row invalid qty
        data.add(new Object[]{
            "QA_multi_row_qty", "SUBMIT", "Multi row validate", "2026-02-28", "High", "Automation note",
            "Laptop", "0", "10000000", "Spec", "Note",
            "Mouse", "0", "100000", "Spec row1", "Note row1",
            "", "", "", "", "",
            "ERROR", "purchaseDetailCreateRequests1.quantity", "Quantity phải lớn hơn 0"
        });

        // 8. Multi row missing asset type
        data.add(new Object[]{
            "QA_multi_row_no_type", "SUBMIT", "Missing asset type in row1", "2026-02-28", "High", "Automation note",
            "Laptop", "1", "10000000", "Spec", "Note",
            null, "1", "100000", "Spec row1", "Note row1",
            "", "", "", "", "",
            "ERROR", "purchaseDetailCreateRequests1.assetTypeId", "Loại tài sản không được để trống"
        });

        ExcelUtils.writeExcelData(filePath, "TestCases", data);
        System.out.println("Restoration complete. Excel file generated at: " + filePath);
    }
}
