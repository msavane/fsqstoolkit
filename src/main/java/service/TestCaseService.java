package service;

import dto.StepDto;
import util.ElementFinder;
import dto.TestCaseDto;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileWriter;
import java.io.IOException;
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
                String locatorType = step.getLocatorType();  // ✅ use the correct getter
                String locatorValue = step.getProperty();    // ✅ property is the locator value
                String value = step.getValue();

                try {
                    //WebElement element = wait.until(ExpectedConditions.elementToBeClickable(resolveLocator(locatorType, locatorValue)));
                    //WebElement element = ElementFinder.findSmart(driver, step.getValue()); // Assuming your helper handles this
                    WebElement element = ElementFinder.findSmart(driver, step.getProperty());

                    switch (action) {
                        case "type":
                            element.clear();
                            element.sendKeys(value);
                            break;
                        case "click":
                            element.click();
                            break;
                        case "keypress":
                            element = ElementFinder.findSmart(driver, step.getProperty()); // 🔥 FIX: use locator (like "search")
                            Keys key = getKeyFromString(value); // value is something like "ENTER"
                            if (key != null) {
                                System.out.printf("⌨️ Pressing key '%s' in element '%s'%n", value, step.getProperty());
                                element.sendKeys(key);
                            } else {
                                System.out.printf("⚠️ Unknown key '%s'%n", value);
                            }
                            break;
                        case "select":
                            System.out.println("🔧 Select not implemented.");
                            break;
                        default:
                            System.out.printf("⚠️ Unknown action '%s' on property '%s'%n", action, locatorType);
                    }

                } catch (Exception e) {
                    System.out.printf("❌ Failed to interact with property '%s': %s%n", locatorType, e.getMessage());
                }
            }

            System.out.println("✅ Test ran successfully.");
        } catch (Exception e) {
            System.out.println("❌ Test failed: " + e.getMessage());
        } finally {
            // driver.quit(); // optional
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
                        WebElement element = ElementFinder.findSmart(driver, field);

                        element.clear();
                        element.sendKeys(value);
                    }

                } else if (step.startsWith("keypress")) {
                    // Example step: keypress "ENTER" key in "search"
                    String[] parts = step.replace("keypress", "").replace("key", "").trim().split(" in ");
                    if (parts.length == 2) {
                        String keyName = parts[0].trim().replace("\"", "").toUpperCase(); // ENTER
                        String field = parts[1].trim().replace("\"", "");

                        WebElement element = ElementFinder.findSmart(driver, field);

                        Keys key = getKeyFromString(keyName);
                        if (key != null) {
                            element.sendKeys(key);
                        } else {
                            System.out.printf("⚠️ Unknown key '%s'%n", keyName);
                        }
                    } else {
                        System.out.println("⚠️ Invalid keypress step format");
                    }


            } else if (step.startsWith("click")) {
                    // your existing click logic here...

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
            // driver.quit(); // optional
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
            //do i call the locator type elementfinder here ?
            System.out.printf("  %d. [%s] using [%s=%s] => %s%n", i++, step.getAction(), step.getLocatorType(), step.getProperty(), step.getValue());
        }

        System.out.printf("%n🎯 Event Trigger: %s%n", testCase.getEventListener());
        System.out.println("=================================\n");
    }

    public void saveTestCaseToFile(TestCaseDto testCase, String filename, boolean gherkin) {
        try (FileWriter writer = new FileWriter(filename)) {
            if (gherkin) {
                for (String step : testCase.getStepsAsText()) {
                    writer.write(step + System.lineSeparator());
                }
            } else {
                writer.write("Feature: " + testCase.getFeatureName() + "\n");
                writer.write("Target URL: " + testCase.getTargetUrl() + "\n");
                writer.write("Event Trigger: " + testCase.getEventListener() + "\n\n");
                for (StepDto step : testCase.getSteps()) {
                    writer.write(String.format("Action: %s, Locator Type: %s, Locator Value: %s, Value: %s%n",
                            step.getAction(), step.getLocatorType(), step.getProperty(), step.getValue()));
                }
            }
            System.out.printf("✅ Test case saved to file: %s%n", filename);
        } catch (IOException e) {
            System.out.printf("❌ Failed to save test case to file: %s%n", e.getMessage());
        }
    }

    private Keys getKeyFromString(String keyName) {
        try {
            return Keys.valueOf(keyName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private By resolveLocator(String locatorType, String property) {
        switch (locatorType.toLowerCase()) {
            case "id":
                return By.id(property);
            case "name":
                return By.name(property);
            case "css":
                return By.cssSelector(property);
            case "xpath":
                return By.xpath(property);
            case "alt":
                return By.cssSelector("img[alt='" + property + "']");
            case "tag":
                return By.tagName(property);
            default:
                throw new IllegalArgumentException("❌ Unsupported locator type: " + locatorType);
        }
    }
}
