package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SectionControllerSystemTest {

    public static final String CHROME_DRIVER_FILE_LOCATION = "chromedriver-mac-arm64/chromedriver";
    public static final String URL = "http://localhost:3000";
    public static final int SLEEP_DURATION = 1000; // 1 second.

    WebDriver driver;

    @BeforeEach
    public void setUpDriver() throws Exception {
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(ops);

        driver.get(URL);
        Thread.sleep(SLEEP_DURATION);
    }

    @AfterEach
    public void terminateDriver() {
        if (driver != null) {
            driver.close();
            driver.quit();
            driver = null;
        }
    }

    @Test
    public void systemTestAddSection() throws Exception {
        WebElement we = driver.findElement(By.id("sections"));
        we.click();
        Thread.sleep(SLEEP_DURATION);

        driver.findElement(By.id("scourseId")).sendKeys("cst499");
        driver.findElement(By.id("syear")).sendKeys("2024");
        driver.findElement(By.id("ssemester")).sendKeys("Fall");
        driver.findElement(By.id("search")).click();
        Thread.sleep(SLEEP_DURATION);

        try {
            while (true) {
                WebElement row499 = driver.findElement(By.xpath("//tr[td='cst499']"));
                List<WebElement> buttons = row499.findElements(By.tagName("button"));
                assertEquals(2, buttons.size());
                buttons.get(1).click();
                Thread.sleep(SLEEP_DURATION);

                List<WebElement> confirmButtons = driver
                        .findElement(By.className("react-confirm-alert-button-group"))
                        .findElements(By.tagName("button"));
                assertEquals(2, confirmButtons.size());
                confirmButtons.get(0).click();
                Thread.sleep(SLEEP_DURATION);
            }
        } catch (NoSuchElementException e) {
            // do nothing, continue with test
        }

        driver.findElement(By.id("addSection")).click();
        Thread.sleep(SLEEP_DURATION);

        driver.findElement(By.id("ecourseId")).sendKeys("cst499");
        driver.findElement(By.id("esecId")).sendKeys("1");
        driver.findElement(By.id("eyear")).sendKeys("2024");
        driver.findElement(By.id("esemester")).sendKeys("Fall");
        driver.findElement(By.id("ebuilding")).sendKeys("052");
        driver.findElement(By.id("eroom")).sendKeys("104");
        driver.findElement(By.id("etimes")).sendKeys("W F 1:00-2:50 pm");
        driver.findElement(By.id("einstructorEmail")).sendKeys("jgross@csumb.edu");
        driver.findElement(By.id("save")).click();
        Thread.sleep(SLEEP_DURATION);

        String message = driver.findElement(By.id("addMessage")).getText();
        assertTrue(message.startsWith("section added"));

        WebElement row499 = driver.findElement(By.xpath("//tr[td='cst499']"));
        List<WebElement> buttons = row499.findElements(By.tagName("button"));
        assertEquals(2, buttons.size());
        buttons.get(1).click();
        Thread.sleep(SLEEP_DURATION);

        List<WebElement> confirmButtons = driver
                .findElement(By.className("react-confirm-alert-button-group"))
                .findElements(By.tagName("button"));
        assertEquals(2, confirmButtons.size());
        confirmButtons.get(0).click();
        Thread.sleep(SLEEP_DURATION);

        assertThrows(NoSuchElementException.class, () ->
                driver.findElement(By.xpath("//tr[td='cst499']")));
    }

    @Test
    public void systemTestAddSectionBadCourse() throws Exception {
        WebElement we = driver.findElement(By.id("sections"));
        we.click();
        Thread.sleep(SLEEP_DURATION);

        driver.findElement(By.id("scourseId")).sendKeys("cst");
        driver.findElement(By.id("syear")).sendKeys("2024");
        driver.findElement(By.id("ssemester")).sendKeys("Fall");
        driver.findElement(By.id("search")).click();
        Thread.sleep(SLEEP_DURATION);

        try {
            while (true) {
                WebElement row499 = driver.findElement(By.xpath("//tr[td='cst499']"));
                List<WebElement> buttons = row499.findElements(By.tagName("button"));
                assertEquals(2, buttons.size());
                buttons.get(1).click();
                Thread.sleep(SLEEP_DURATION);

                List<WebElement> confirmButtons = driver
                        .findElement(By.className("react-confirm-alert-button-group"))
                        .findElements(By.tagName("button"));
                assertEquals(2, confirmButtons.size());
                confirmButtons.get(0).click();
                Thread.sleep(SLEEP_DURATION);
            }
        } catch (NoSuchElementException e) {
            // do nothing, continue with test
        }

        driver.findElement(By.id("addSection")).click();
        Thread.sleep(SLEEP_DURATION);

        driver.findElement(By.id("ecourseId")).sendKeys("cst599");
        driver.findElement(By.id("esecId")).sendKeys("1");
        driver.findElement(By.id("eyear")).sendKeys("2024");
        driver.findElement(By.id("esemester")).sendKeys("Fall");
        driver.findElement(By.id("ebuilding")).sendKeys("052");
        driver.findElement(By.id("eroom")).sendKeys("104");
        driver.findElement(By.id("etimes")).sendKeys("W F 1:00-2:50 pm");
        driver.findElement(By.id("einstructorEmail")).sendKeys("jgross@csumb.edu");
        driver.findElement(By.id("save")).click();
        Thread.sleep(SLEEP_DURATION);

        WebElement msg = driver.findElement(By.id("addMessage"));
        String message = msg.getText();
        assertEquals("course not found cst599", message);
    }
}
