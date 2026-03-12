package com.assetmanagement.test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;

public class AssetTypeTest {

    WebDriver driver;

    @BeforeClass
    public void setup() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.get("http://localhost:8080/manager/asset-types");
    }

    // Hàm tạo độ trễ để nhìn rõ thao tác gõ/chọn
    private void delay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void fillForm(String name,
                          String description,
                          String spec,
                          String model,
                          String typeClass,
                          String depreciationMethod,
                          String usefulLife) {

        driver.findElement(By.name("typeName")).clear();
        driver.findElement(By.name("typeName")).sendKeys(name);
        delay(400);

        driver.findElement(By.name("description")).clear();
        driver.findElement(By.name("description")).sendKeys(description);
        delay(400);

        driver.findElement(By.name("specification")).clear();
        driver.findElement(By.name("specification")).sendKeys(spec);
        delay(400);

        driver.findElement(By.name("model")).clear();
        driver.findElement(By.name("model")).sendKeys(model);
        delay(400);

        new Select(driver.findElement(By.name("categoryId")))
                .selectByIndex(1);
        delay(400);

        new Select(driver.findElement(By.name("typeClass")))
                .selectByValue(typeClass);
        delay(400);

        new Select(driver.findElement(By.name("defaultDepreciationMethod")))
                .selectByValue(depreciationMethod);
        delay(400);

        WebElement usefulLifeInput =
                driver.findElement(By.name("defaultUsefulLifeMonths"));
        usefulLifeInput.clear();
        usefulLifeInput.sendKeys(usefulLife);
        delay(600); // Dừng 0.6s trước khi bấm Create

    }

    private void clickCreate() {
        driver.findElement(By.id("btnCreate")).click();
    }

    // Tự động dừng 2.5 giây sau MỖI test case để bạn đọc thông báo
    @AfterMethod
    public void pauseAfterTest() {
        delay(2500);
    }

    //TEST CASES

    @Test(priority = 1)
    public void TC01_CreateValidAssetType() {
        fillForm("Laptop Office",
                "Laptop dùng cho văn phòng",
                "Core i5, 8GB RAM",
                "Dell 5400",
                "EQUIPMENT",
                "STRAIGHT_LINE",
                "36");

        clickCreate();

        Assert.assertTrue(driver.getPageSource()
                .contains("Laptop Office"));
    }

    @Test(priority = 2)
    public void TC02_CreatePrinter() {
        fillForm("Printer Laser",
                "Máy in phòng kế toán",
                "In 2 mặt",
                "HP 402dn",
                "TOOL",
                "STRAIGHT_LINE",
                "24");

        clickCreate();

        Assert.assertTrue(driver.getPageSource()
                .contains("Printer Laser"));
    }

    @Test(priority = 3)
    public void TC03_CreateCameraDeclining() {
        fillForm("Security Camera",
                "Camera giám sát",
                "Full HD",
                "Hikvision",
                "EQUIPMENT",
                "DECLINING_BALANCE",
                "48");

        clickCreate();

        Assert.assertTrue(driver.getPageSource()
                .contains("Security Camera"));
    }

    @Test(priority = 4)
    public void TC04_EmptyName() {
        fillForm("",
                "Test thiếu tên",
                "Spec",
                "Model X",
                "EQUIPMENT",
                "STRAIGHT_LINE",
                "36");

        clickCreate();

        Assert.assertTrue(driver.getPageSource()
                .contains("Tên loại tài sản không được để trống"));
    }

    @Test(priority = 5)
    public void TC05_UsefulLifeZero() {
        fillForm("Meeting Room TV",
                "TV phòng họp",
                "4K",
                "Samsung",
                "EQUIPMENT",
                "STRAIGHT_LINE",
                "0");

        clickCreate();

        Assert.assertTrue(driver.getPageSource()
                .contains("Thời gian sử dụng phải lớn hơn 0 tháng"));
    }

    @Test(priority = 6)
    public void TC06_UsefulLifeTooLarge() {
        fillForm("Warehouse Camera",
                "Camera kho",
                "HD",
                "Xiaomi",
                "EQUIPMENT",
                "STRAIGHT_LINE",
                "700");

        clickCreate();

        Assert.assertTrue(driver.getPageSource()
                .contains("không hợp lệ"));
    }

    @Test(priority = 7)
    public void TC07_DuplicateName() {
        fillForm("Laptop Office",
                "Duplicate test",
                "Core i5",
                "Dell",
                "EQUIPMENT",
                "STRAIGHT_LINE",
                "36");

        clickCreate();

        Assert.assertTrue(driver.getPageSource()
                .contains("Ten loai tai san da ton tai"));
    }

    @Test(priority = 8)
    public void TC08_EmptyCategory() {

        driver.findElement(By.name("typeName")).sendKeys("Barcode Scanner");
        delay(400);

        new Select(driver.findElement(By.name("typeClass")))
                .selectByValue("TOOL");
        delay(400);

        new Select(driver.findElement(By.name("defaultDepreciationMethod")))
                .selectByValue("STRAIGHT_LINE");
        delay(400);

        driver.findElement(By.name("defaultUsefulLifeMonths"))
                .sendKeys("24");
        delay(600);

        clickCreate();

        Assert.assertTrue(driver.getPageSource()
                .contains("Danh mục không được để trống"));
    }

    @AfterClass
    public void teardown() {
        delay(2000);
        driver.quit();
    }
}