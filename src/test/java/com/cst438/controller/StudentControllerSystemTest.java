package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

public class StudentControllerSystemTest {

    public static final String CHROME_DRIVER_FILE_LOCATION =
            "chromedriver-mac-arm64/chromedriver";
    public static final String URL = "http://localhost:3000";
    public static final int SLEEP_DURATION = 1000; // 1 second.

    WebDriver driver;

    @BeforeEach
    public void setUpDriver() throws Exception {
        // set properties required by Chrome Driver
        System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");

        // start the driver
        driver = new ChromeDriver(ops);

        driver.get(URL);
        // must have a short wait to allow time for the page to download
        Thread.sleep(SLEEP_DURATION);
    }

    @AfterEach
    public void terminateDriver() {
        if (driver != null) {
            // quit driver
            driver.close();
            driver.quit();
            driver = null;
        }
    }

    @Test
    public void systemTestStudentAddCourse() throws Exception {
        // Navigate to the "Enroll in a class" page
        WebElement we = driver.findElement(By.xpath("//a[@href='/addCourse']"));
        we.click();
        Thread.sleep(SLEEP_DURATION);

        // Locate the course row
        List<WebElement> courseRows = driver.findElements(By.xpath("//tr"));
        WebElement targetCourseRow = null;
        for (WebElement row : courseRows) {
            if (row.getText().contains("2024") && row.getText().contains("Fall") && row.getText().contains("cst438")) {
                targetCourseRow = row;
                break;
            }
        }

        // If the course row is found, click the "Add Course" button
        if (targetCourseRow != null) {
            WebElement enrollButton = targetCourseRow.findElement(By.xpath(".//button[text()='Add Course']"));
            enrollButton.click();
            Thread.sleep(SLEEP_DURATION);

            // Handle the confirmation alert
            Alert alert = driver.switchTo().alert();
            String message = alert.getText();
            alert.accept();
            assertEquals("Enrolled successfully!", message);
        }
    }
}
