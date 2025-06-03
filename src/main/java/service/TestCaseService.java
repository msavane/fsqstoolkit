package service;

import dto.StepDto;
import dto.TestCaseDto;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class TestCaseService {

    public void runTestCase(TestCaseDto testCase) {
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            driver.get(testCase.getTargetUrl());

            for (StepDto step : testCase.getSteps()) {
                String action = step.getAction().toLowerCase();
                String propertyName = step.getProperty();
                String value = step.getValue();

                try {
                    WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.name(propertyName)));

                    switch (action) {
                        case "type":
                            element.clear();
                            element.sendKeys(value);
                            break;

                        case "click":
                            element.click();
                            break;

                        case "select":
                            System.out.println("🔧 Select action not yet implemented.");
                            break;

                        case "keypress":
                            Keys key = getKeyFromString(value);
                            if (key != null) {
                                element.sendKeys(key);
                            } else {
                                System.out.printf("⚠️ Unknown key '%s'%n", value);
                            }
                            break;

                        default:
                            System.out.printf("⚠️ Unknown action '%s' on property '%s'%n", action, propertyName);
                    }

                } catch (Exception e) {
                    System.out.printf("❌ Failed to interact with element named '%s': %s%n", propertyName, e.getMessage());
                }
            }

            // Trigger final submit
            try {
                WebElement triggerElement = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
                triggerElement.click();
                System.out.println("✅ Submit button clicked.");
            } catch (Exception e) {
                System.out.printf("⚠️ Submit button not found: %s%n", e.getMessage());
            }

            System.out.println("✅ Test ran successfully.");
        } catch (Exception e) {
            System.out.println("❌ Test failed: " + e.getMessage());
        } finally {
            // Optional: Keep the browser open for debugging
            // driver.quit();
        }
    }

    public void runGherkinStyleTest(TestCaseDto testCase) {
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            for (String step : testCase.getStepsAsText()) {
                if (step.startsWith("navigate to")) {
                    String url = step.replace("navigate to", "").trim();
                    driver.get(url);

                } else if (step.startsWith("enter")) {
                    String[] parts = step.replace("enter", "").trim().split(" into ");
                    if (parts.length == 2) {
                        String value = parts[0].trim().replace("\"", "");
                        String field = parts[1].trim().replace("\"", "");
                        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.name(field)));
                        element.clear();
                        element.sendKeys(value);
                    }

                } else if (step.startsWith("click")) {
                    String field = step.replace("click", "").replace("button", "").trim().replace("\"", "");
                    WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.name(field)));
                    button.click();

                } else if (step.startsWith("should see")) {
                    String expected = step.replace("should see", "").trim().replace("\"", "");
                    boolean found = driver.getPageSource().contains(expected);
                    if (!found) throw new AssertionError("Expected text not found: " + expected);
                }
            }

            System.out.println("✅ Gherkin-style test ran successfully.");
        } catch (Exception e) {
            System.out.println("❌ Gherkin-style test failed: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }

    public void printTestCaseSummary(TestCaseDto testCase) {
        System.out.println("\n======= TEST CASE SUMMARY =======");
        System.out.printf("🧪 Feature:         %s%n", testCase.getFeatureName());
        System.out.printf("🌐 Target URL:      %s%n%n", testCase.getTargetUrl());

        System.out.println("🔁 Steps:");
        List<StepDto> steps = testCase.getSteps();
        int i = 1;
        for (StepDto step : steps) {
            System.out.printf("  %d. [%s]    %-10s => %s%n", i++, step.getAction(), step.getProperty(), step.getValue());
        }

        System.out.printf("%n🎯 Event Trigger: %s%n", testCase.getEventListener());
        System.out.println("=================================\n");
        System.out.print("Run this test case now? (y/n): ");
    }

    private Keys getKeyFromString(String keyName) {
        try {
            return Keys.valueOf(keyName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
