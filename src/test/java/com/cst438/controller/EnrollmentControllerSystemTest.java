package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class EnrollmentControllerSystemTest {

    public static final String CHROME_DRIVER_FILE_LOCATION =
            "chromedriver-mac-arm64/chromedriver";

    //public static final String CHROME_DRIVER_FILE_LOCATION =
    //        "~/chromedriver_macOS/chromedriver";
    public static final String URL = "http://localhost:3000";

    public static final int SLEEP_DURATION = 1000; // 1 second.


    // add selenium dependency to pom.xml

    // these tests assumes that test data does NOT contain any
    // sections for course cst499 in 2024 Spring term.

    WebDriver driver;

    @BeforeEach
    public void setUpDriver() throws Exception {

        // set properties required by Chrome Driver
        System.setProperty(
                "webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
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
    public void systemTestInstructorModifyGrade() throws Exception {

        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        Thread.sleep(SLEEP_DURATION);
        driver.findElement(By.xpath("//a[@href='/sections']")).click();

        WebElement courseRow = driver.findElement(By.xpath("//tr[td='8']"));
        WebElement enrollments = courseRow.findElement(By.xpath("//a[@href='/enrollments']"));
        enrollments.click();

        WebElement enrollmentRow = driver.findElement(By.xpath("//tr[td='thomas edison']"));
        WebElement gradeBox = enrollmentRow.findElement(By.tagName("input"));
        gradeBox.clear();
        Thread.sleep(SLEEP_DURATION);

        gradeBox.sendKeys("F");
        driver.findElement(By.tagName("Button")).click();
        Thread.sleep(SLEEP_DURATION);
        String message = driver.findElement(By.xpath("//h3[text()='Grades saved']")).getText();
        assertEquals("Grades saved", message);
    }
}
