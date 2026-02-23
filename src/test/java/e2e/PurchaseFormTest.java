package e2e;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.*;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class PurchaseFormTest {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String DRIVER_PATH = "D:\\drivers\\edgedriver\\msedgedriver.exe";
    private static final String BASE_URL = "http://localhost:8080/asm";
    private static final String FORM_URL = BASE_URL + "/asset-manager/purchase-form";

    private static final boolean DEMO_MODE = true;
    private static final int STEP_DELAY_MS = 1000;

    private void sleep(int ms) {
        if (!DEMO_MODE) return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }

    @BeforeEach
    void setup() {
        System.setProperty("webdriver.edge.driver", DRIVER_PATH);
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--start-maximized");
        driver = new EdgeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void teardown() {
        if (driver != null) driver.quit();
    }

    private void open() {
        driver.get(FORM_URL);
        wait.until(ExpectedConditions.urlContains("/purchase-form"));
        sleep(STEP_DELAY_MS);
    }

    private WebElement el(String idOrName) {
        List<WebElement> byId = driver.findElements(By.id(idOrName));
        if (!byId.isEmpty()) return byId.get(0);
        List<WebElement> byName = driver.findElements(By.name(idOrName));
        if (!byName.isEmpty()) return byName.get(0);
        List<WebElement> byNameEnds = driver.findElements(By.cssSelector(
                "[name$='." + idOrName + "'], [name$='[" + idOrName + "]'], [name$='" + idOrName + "']"
        ));
        if (!byNameEnds.isEmpty()) return byNameEnds.get(0);

        if ("neededByDate".equals(idOrName)) {
            List<WebElement> dates = driver.findElements(By.cssSelector("input[type='date']"));
            if (!dates.isEmpty()) return dates.get(0);
        }

        throw new NoSuchElementException("Cannot find element: " + idOrName);
    }

    private void type(String idOrName, String value) {
        WebElement e = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//*[@id='" + idOrName + "'] | //*[@name='" + idOrName + "']")));
        e.click();
        e.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        if (value == null) value = "";
        e.sendKeys(value);
        sleep(500);
    }

    private void setDateJs(String idOrName, String yyyyMMdd) {
        WebElement d = el(idOrName);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                d, yyyyMMdd
        );
        sleep(500);
    }

    private void pickText(String selectId, String text) {
        if (text == null || text.isBlank()) return;
        WebElement e = el(selectId);
        new Select(e).selectByVisibleText(text);
        sleep(500);
    }

    private void pickIdx(String selectId, int index) {
        WebElement e = el(selectId);
        new Select(e).selectByIndex(index);
        sleep(500);
    }

    private void clickSubmit() {
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-solid[value='save']"))).click();
        sleep(STEP_DELAY_MS);
    }

    private void clickDraft() {
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-outline[value='draft']"))).click();
        sleep(STEP_DELAY_MS);
    }

    private void submitJs() {
        WebElement form = driver.findElement(By.tagName("form"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].submit();", form);
        sleep(STEP_DELAY_MS);
    }

    private void addRow() {
        wait.until(ExpectedConditions.elementToBeClickable(By.name("addDetail"))).click();
        sleep(STEP_DELAY_MS);
    }

    private int rowCount() {
        return driver.findElements(By.xpath("//select[contains(@name,'.assetTypeId')]")).size();
    }

    private void fillItem(int index, String type, String qty, String price, String spec, String note) {
        if (type == null || type.isBlank()) return;
        String prefix = "purchaseDetailCreateRequests" + index + ".";
        try {
            pickText(prefix + "assetTypeId", type);
        } catch (Exception ex) {
            pickIdx(prefix + "assetTypeId", 1);
        }
        if (qty != null && !qty.isBlank()) type(prefix + "quantity", qty);
        if (price != null && !price.isBlank()) type(prefix + "estimatePrice", price);
        if (spec != null && !spec.isBlank()) type(prefix + "specificationRequirement", spec);
        if (note != null && !note.isBlank()) type(prefix + "note", note);
    }

    private String errText(String idOrName) {
        WebElement field = el(idOrName);
        List<WebElement> divs = field.findElements(By.xpath("ancestor::td[1]//div | ancestor::div[contains(@class,'form-group')]//div"));
        for (WebElement d : divs) {
            String t = d.getText() == null ? "" : d.getText().trim();
            if (!t.isEmpty() && !t.contains(idOrName)) return t;
        }
        return "";
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("excelDataProvider")
    void purchase_form_data_driven_test(
            String testId, String action, String reason, String date, String priority, String note,
            String i1_type, String i1_qty, String i1_price, String i1_spec, String i1_note,
            String i2_type, String i2_qty, String i2_price, String i2_spec, String i2_note,
            String i3_type, String i3_qty, String i3_price, String i3_spec, String i3_note,
            String expectedOutcome, String errField, String expectedVal) {

        String resultStatus = "FALSE";
        try {
            open();
            
            // Handle logic-only tests
            if ("REMOVE_ROW".equalsIgnoreCase(action)) {
                int before = rowCount();
                if (before < 2) { addRow(); before = rowCount(); }
                wait.until(ExpectedConditions.elementToBeClickable(By.className("btn-remove"))).click();
                sleep(1000);
                assertEquals(before - 1, rowCount(), "Row count should decrease");
                resultStatus = "TRUE";
                return;
            }

            // Fill General
            type("reason", reason);
            if (date != null && !date.isBlank()) setDateJs("neededByDate", date);
            if (priority != null && !priority.isBlank()) {
                if ("null".equalsIgnoreCase(priority)) {
                    // Do nothing to test missing priority
                } else {
                    pickText("priority", priority);
                }
            }
            type("note", note);

            // Fill Items
            fillItem(0, i1_type, i1_qty, i1_price, i1_spec, i1_note);
            
            if (i2_type != null && !i2_type.isBlank()) {
                addRow();
                fillItem(1, i2_type, i2_qty, i2_price, i2_spec, i2_note);
            }
            if (i3_type != null && !i3_type.isBlank()) {
                addRow();
                fillItem(2, i3_type, i3_qty, i3_price, i3_spec, i3_note);
            }

            // Action
            if ("SUBMIT".equalsIgnoreCase(action)) clickSubmit();
            else if ("DRAFT".equalsIgnoreCase(action)) clickDraft();
            else if ("SUBMIT_JS".equalsIgnoreCase(action)) submitJs();

            // Verify outcome
            if ("ERROR".equalsIgnoreCase(expectedOutcome)) {
                assertTrue(driver.getCurrentUrl().contains("/purchase-form"), "Should stay on form");
                if (errField != null && !errField.isBlank()) {
                    String actual = errText(errField);
                    if (expectedVal != null && !expectedVal.isBlank()) {
                        assertEquals(expectedVal, actual);
                    } else {
                        assertFalse(actual.isBlank(), "Expected error for " + errField);
                    }
                }
            } else if ("REDIRECT".equalsIgnoreCase(expectedOutcome)) {
                wait.until(ExpectedConditions.urlContains(expectedVal));
                assertTrue(driver.getCurrentUrl().contains(expectedVal));
            } else if ("SUCCESS_NO_400".equalsIgnoreCase(expectedOutcome)) {
                assertFalse(driver.getPageSource().contains("HTTP Status 400"));
            }
            
            resultStatus = "TRUE"; // If we reach here, it passed
        } catch (Throwable t) {
            resultStatus = "FALSE";
            throw t;
        } finally {
            try {
                e2e.utils.ExcelUtils.updateResultColumn("src/test/resources/PurchaseFormTestData.xlsx", "TestCases", testId, resultStatus);
            } catch (Exception e) {
                System.err.println("Failed to update Excel: " + e.getMessage());
            }
        }
    }

    static Stream<Arguments> excelDataProvider() throws Exception {
        String path = "src/test/resources/PurchaseFormTestData.xlsx";
        return e2e.utils.ExcelUtils.readExcelData(path, "TestCases").stream()
                .map(Arguments::of);
    }
}