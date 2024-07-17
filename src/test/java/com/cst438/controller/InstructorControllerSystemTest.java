package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InstructorControllerSystemTest {

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
    public void systemTestInstructorGradesAssignment() throws Exception {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.linkText("Show Sections")).click();
        Thread.sleep(SLEEP_DURATION);

        WebElement assignmentsLink = driver.findElement(By.linkText("Assignments"));
        assignmentsLink.click();
        Thread.sleep(SLEEP_DURATION);

        WebElement assignmentRow = driver.findElement(By.xpath("//tr[td[contains(text(),'db homework 1')]]"));
        List<WebElement> buttons = assignmentRow.findElements(By.tagName("button"));
        buttons.get(0).click();  // assuming the "Grade" button is the first button
        Thread.sleep(SLEEP_DURATION);

        List<WebElement> gradeInputs = driver.findElements(By.name("score"));
        for (WebElement gradeInput : gradeInputs) {
            gradeInput.clear();
            gradeInput.sendKeys("90"); // example grade
        }
        Thread.sleep(SLEEP_DURATION);

        driver.findElement(By.xpath("//button[contains(text(),'Save')]")).click();
        Thread.sleep(SLEEP_DURATION);

        String message = driver.findElement(By.xpath("//h4[contains(text(),'Grades saved')]")).getText();
        assertTrue(message.startsWith("Grades saved"));

        driver.findElement(By.xpath("//button[contains(text(),'Close')]")).click();
    }
}
