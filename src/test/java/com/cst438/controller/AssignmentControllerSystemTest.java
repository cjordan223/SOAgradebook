package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.jupiter.api.Assertions.*;

public class AssignmentControllerSystemTest {
    public static final String CHROME_DRIVER_FILE_LOCATION = "chromedriver-mac-arm64/chromedriver";
    public static final String URL = "http://localhost:3000";
    public static final int SLEEP_DURATION = 2000; // Increased to 2 seconds for network delay.

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
    public void systemTestAddAssignment() throws Exception {
        // Verify the section is found in the list and click into assignments
        // Section should be in list because it exists in data.sql
        // And instructor cannot delete
        try {
            driver.findElement(By.id("year")).sendKeys("2024");
            driver.findElement(By.id("semester")).sendKeys("Fall");
            driver.findElement(By.linkText("Show Sections")).click();
            Thread.sleep(SLEEP_DURATION);

            // Click on the "Assignments" link for the relevant section
            WebElement assignmentsLink = driver.findElement(By.linkText("Assignments"));
            assignmentsLink.click();
            Thread.sleep(SLEEP_DURATION);

            // Click to add Assignment
            driver.findElement(By.xpath("//button[contains(@class, 'add-assignment-button')]")).click();
            Thread.sleep(SLEEP_DURATION);

            // Enter in assignment values
            driver.findElement(By.name("title")).sendKeys("Testing Assignment");
            driver.findElement(By.name("dueDate")).sendKeys("2024-09-01");
            driver.findElement(By.xpath("//button[contains(text(), 'Save')]")).click();
            Thread.sleep(SLEEP_DURATION);

            // Check if message shows assignment created
            WebElement msg = driver.findElement(By.tagName("h4"));
            String message = msg.getText();
            System.out.println("Message displayed: " + message); // Debug log

            assertTrue(message.contains("Assignment created"));
        } catch (NoSuchElementException e) {
            System.out.println("Error: Element not found - " + e.getMessage());
            fail("Test failed due to missing element.");
        } catch (InterruptedException e) {
            System.out.println("Error: Thread interrupted - " + e.getMessage());
            fail("Test failed due to interruption.");
        }
    }
}
