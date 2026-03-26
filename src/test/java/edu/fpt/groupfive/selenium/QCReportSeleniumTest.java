package edu.fpt.groupfive.selenium;


import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class QCReportSeleniumTest {

//    private static WebDriver driver;
//    private static WebDriverWait wait;
//
//    private static final String BASE_URL = "http://localhost:8080";
//    private static final String LIST_URL  = BASE_URL + "/qc-reports/list";
//
//    // ID của report được tạo trong TC01 — dùng lại cho TC03, TC04
//    private static int createdReportId = -1;
//
//    // ==================== SETUP / TEARDOWN ====================
//
//    @BeforeAll
//    static void setupDriver() {
//        System.setProperty("webdriver.edge.driver", "C:\\Users\\dhmta\\Downloads\\msedgedriver.exe");
//        EdgeOptions options = new EdgeOptions();
//        options.addArguments("--headless=new");   // bỏ dòng này nếu muốn xem trình duyệt
//        options.addArguments("--no-sandbox");
//        options.addArguments("--disable-dev-shm-usage");
//        options.addArguments("--window-size=1280,900");
//
//        driver = new EdgeDriver(options);
//        wait   = new WebDriverWait(driver, Duration.ofSeconds(10));
//    }
//
//    @AfterAll
//    static void tearDown() {
//        if (driver != null) driver.quit();
//    }
//
//    // ==================== HELPER METHODS ====================
//
//    /** Chờ đến khi flash message xuất hiện, trả về text của nó. */
//    private String waitForFlashMessage(String cssClass) {
//        By locator = By.cssSelector(".alert." + cssClass);
//        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
//        return alert.getText();
//    }
//
//    /** Chọn option trong <select> theo visible text. */
//    private void selectByText(By locator, String text) {
//        Select select = new Select(wait.until(ExpectedConditions.elementToBeClickable(locator)));
//        select.selectByVisibleText(text);
//    }
//
//    /** Lấy report ID từ URL hiện tại: /qc-reports/{id} */
//    private int extractIdFromCurrentUrl() {
//        String url = driver.getCurrentUrl();           // http://localhost:8080/qc-reports/42
//        String[] parts = url.split("/");
//        return Integer.parseInt(parts[parts.length - 1]);
//    }
//
//    /** Điền form tạo / sửa báo cáo. */
//    private void fillReportForm(String assetText, String status, String inspectorText, String note) {
//        selectByText(By.id("assetId"),      assetText);
//        selectByText(By.id("status"),       status);
//        selectByText(By.id("inspectedBy"),  inspectorText);
//
//        WebElement noteField = driver.findElement(By.id("note"));
//        noteField.clear();
//        noteField.sendKeys(note);
//    }
//
//    // ==================== TEST CASES ====================
//
//    /**
//     * TC01 — Create: tạo báo cáo QC thành công.
//     * Kỳ vọng: redirect sang detail page, hiện flash "thành công".
//     */
//    @Test
//    @Order(1)
//    @DisplayName("TC01 - Tạo báo cáo QC thành công")
//    void testCreateQCReport_Success() {
//        driver.get(BASE_URL + "/qc-reports/create");
//
//        fillReportForm(
//                "Máy in Canon (#5)",   // assetText  — sửa theo data thật
//                "PENDING",             // status
//                "Nguyễn Văn A (#12)",  // inspectorText — sửa theo data thật
//                "Selenium test - tạo báo cáo tự động"
//        );
//
//        driver.findElement(By.cssSelector("button[type='submit']")).click();
//
//        // Sau khi tạo thành công, controller redirect -> /qc-reports/{id}
//        wait.until(ExpectedConditions.urlMatches(".*/qc-reports/\\d+$"));
//
//        createdReportId = extractIdFromCurrentUrl();
//        assertTrue(createdReportId > 0, "Report ID phải > 0 sau khi tạo");
//
//        String msg = waitForFlashMessage("alert-success");
//        assertTrue(msg.contains("thành công"), "Flash message phải chứa 'thành công'");
//    }
//
//    /**
//     * TC02 — Create validation: bỏ trống asset, kỳ vọng hiện flash error.
//     */
//    @Test
//    @Order(2)
//    @DisplayName("TC02 - Tạo báo cáo QC thiếu asset → báo lỗi")
//    void testCreateQCReport_MissingAsset() {
//        driver.get(BASE_URL + "/qc-reports/create");
//
//        // Không chọn asset, chỉ chọn status và inspector
//        selectByText(By.id("status"),      "PASSED");
//        selectByText(By.id("inspectedBy"), "Nguyễn Văn A (#12)");
//
//        driver.findElement(By.cssSelector("button[type='submit']")).click();
//
//        // Redirect về /create kèm flash error
//        wait.until(ExpectedConditions.urlContains("/qc-reports/create"));
//
//        String err = waitForFlashMessage("alert-error");
//        assertFalse(err.isBlank(), "Phải có thông báo lỗi khi thiếu asset");
//    }
//
//    /**
//     * TC03 — Read / List: mở trang list, kiểm tra bảng hiển thị đúng.
//     */
//    @Test
//    @Order(3)
//    @DisplayName("TC03 - Danh sách báo cáo QC hiển thị đúng")
//    void testListQCReports() {
//        driver.get(LIST_URL);
//
//        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table")));
//
//        // Bảng phải có ít nhất 1 hàng dữ liệu
//        java.util.List<WebElement> rows = driver.findElements(By.cssSelector("tbody tr"));
//        assertFalse(rows.isEmpty(), "Bảng phải có ít nhất 1 báo cáo");
//
//        // Kiểm tra các cột header
//        java.util.List<WebElement> headers = driver.findElements(By.cssSelector("thead th"));
//        assertTrue(headers.size() >= 6, "Bảng phải có ít nhất 6 cột");
//    }
//
//    /**
//     * TC04 — Read / List filter: lọc theo status PASSED.
//     */
//    @Test
//    @Order(4)
//    @DisplayName("TC04 - Lọc danh sách theo trạng thái PASSED")
//    void testListQCReports_FilterByStatus() {
//        driver.get(LIST_URL);
//
//        selectByText(By.cssSelector("select[name='status']"), "PASSED");
//
//        // Đợi page reload
//        wait.until(ExpectedConditions.urlContains("status=PASSED"));
//
//        // Tất cả badge trong bảng phải là PASSED
//        java.util.List<WebElement> badges = driver.findElements(By.cssSelector(".badge"));
//        for (WebElement badge : badges) {
//            assertEquals("PASSED", badge.getText().trim(),
//                    "Tất cả báo cáo hiển thị phải có trạng thái PASSED");
//        }
//    }
//
//    /**
//     * TC05 — Read: xem chi tiết báo cáo vừa tạo ở TC01.
//     */
//    @Test
//    @Order(5)
//    @DisplayName("TC05 - Xem chi tiết báo cáo QC")
//    void testViewQCReport() {
//        assumeReportCreated();
//
//        driver.get(BASE_URL + "/qc-reports/" + createdReportId);
//
//        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".detail-grid")));
//
//        // Kiểm tra mã báo cáo hiển thị đúng
//        String reportIdText = driver.findElement(
//                By.xpath("//span[@class='detail-label' and text()='Mã báo cáo']/following-sibling::div")
//        ).getText();
//        assertTrue(reportIdText.contains(String.valueOf(createdReportId)),
//                "Detail page phải hiển thị đúng mã báo cáo");
//
//        // Kiểm tra có đủ nút action
//        assertNotNull(driver.findElement(By.xpath("//a[contains(text(),'Chỉnh sửa')]")));
//        assertNotNull(driver.findElement(By.xpath("//button[contains(text(),'Xóa báo cáo')]")));
//    }
//
//    /**
//     * TC06 — Update: sửa trạng thái sang FAILED.
//     */
//    @Test
//    @Order(6)
//    @DisplayName("TC06 - Cập nhật báo cáo QC thành công")
//    void testUpdateQCReport_Success() {
//        assumeReportCreated();
//
//        driver.get(BASE_URL + "/qc-reports/" + createdReportId + "/edit");
//
//        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("status")));
//
//        // Đổi status sang FAILED và cập nhật ghi chú
//        selectByText(By.id("status"), "FAILED");
//
//        WebElement noteField = driver.findElement(By.id("note"));
//        noteField.clear();
//        noteField.sendKeys("Selenium test - cập nhật ghi chú");
//
//        driver.findElement(By.cssSelector("button[type='submit']")).click();
//
//        // Redirect về detail page
//        wait.until(ExpectedConditions.urlMatches(".*/qc-reports/" + createdReportId + "$"));
//
//        String msg = waitForFlashMessage("alert-success");
//        assertTrue(msg.contains("thành công"), "Flash message phải chứa 'thành công'");
//
//        // Kiểm tra badge đã đổi sang FAILED
//        WebElement badge = wait.until(
//                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".badge"))
//        );
//        assertEquals("FAILED", badge.getText().trim(), "Trạng thái phải được cập nhật thành FAILED");
//    }
//
//    /**
//     * TC07 — Update validation: ID không tồn tại → redirect về list kèm error.
//     */
//    @Test
//    @Order(7)
//    @DisplayName("TC07 - Sửa báo cáo ID không tồn tại → báo lỗi")
//    void testUpdateQCReport_NotFound() {
//        driver.get(BASE_URL + "/qc-reports/999999/edit");
//
//        wait.until(ExpectedConditions.urlContains("/qc-reports/list"));
//
//        String err = waitForFlashMessage("alert-error");
//        assertFalse(err.isBlank(), "Phải có thông báo lỗi khi ID không tồn tại");
//    }
//
//    /**
//     * TC08 — Delete: xóa báo cáo vừa tạo, kỳ vọng redirect về list.
//     */
//    @Test
//    @Order(8)
//    @DisplayName("TC08 - Xóa báo cáo QC thành công")
//    void testDeleteQCReport_Success() {
//        assumeReportCreated();
//
//        driver.get(BASE_URL + "/qc-reports/" + createdReportId);
//
//        wait.until(ExpectedConditions.visibilityOfElementLocated(
//                By.xpath("//button[contains(text(),'Xóa báo cáo')]")
//        ));
//
//        // Mở modal xác nhận
//        driver.findElement(By.xpath("//button[contains(text(),'Xóa báo cáo')]")).click();
//
//        // Click nút Xóa trong modal
//        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(
//                By.cssSelector(".modal-actions .btn-danger")
//        ));
//        confirmBtn.click();
//
//        // Redirect về list sau khi xóa
//        wait.until(ExpectedConditions.urlContains("/qc-reports/list"));
//
//        String msg = waitForFlashMessage("alert-success");
//        assertTrue(msg.contains("thành công"), "Flash message phải chứa 'thành công' sau khi xóa");
//
//        // Kiểm tra report đã biến mất khỏi bảng
//        driver.get(BASE_URL + "/qc-reports/" + createdReportId);
//        wait.until(ExpectedConditions.urlContains("/qc-reports/list"));
//    }
//
//    /**
//     * TC09 — Delete: ID không tồn tại → redirect về list kèm error.
//     */
//    @Test
//    @Order(9)
//    @DisplayName("TC09 - Xóa báo cáo ID không tồn tại → báo lỗi")
//    void testDeleteQCReport_NotFound() {
//        // Gửi POST thẳng bằng JS form submit (không có modal)
//        driver.get(LIST_URL);
//        ((JavascriptExecutor) driver).executeScript(
//                "var f=document.createElement('form');" +
//                        "f.method='POST';" +
//                        "f.action='/qc-reports/999999/delete';" +
//                        "document.body.appendChild(f);" +
//                        "f.submit();"
//        );
//
//        wait.until(ExpectedConditions.urlContains("/qc-reports/list"));
//
//        String err = waitForFlashMessage("alert-error");
//        assertFalse(err.isBlank(), "Phải có thông báo lỗi khi xóa ID không tồn tại");
//    }
//
//    // ==================== PRIVATE UTILITIES ====================
//
//    /** Bỏ qua test nếu TC01 chưa tạo được report. */
//    private void assumeReportCreated() {
//        org.junit.jupiter.api.Assumptions.assumeTrue(
//                createdReportId > 0,
//                "Bỏ qua: chưa có createdReportId — TC01 có thể đã fail"
//        );
//    }
}