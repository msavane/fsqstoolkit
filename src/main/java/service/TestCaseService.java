package service;

import dto.StepDto;
import dto.TestCaseDto;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import util.ElementFinder;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestCaseService {

    public void runTestCase(TestCaseDto testCase) {
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            driver.get(testCase.getTargetUrl());

            for (StepDto step : testCase.getSteps()) {
                String action = step.getAction().toLowerCase();
                String locatorValue = step.getProperty();
                String locatorType = step.getLocatorType();
                String value = step.getValue();

                try {
                    WebElement element = ElementFinder.findSmart(driver, locatorValue, locatorType);

                    switch (action) {
                        case "type":
                            element.clear();
                            element.sendKeys(value);
                            break;

                        case "click":
                            element.click();
                            break;

                        case "keypress":
                            Keys key = getKeyFromString(value);
                            if (key != null) element.sendKeys(key);
                            break;

                        case "assert":
                            assertGenericPresence(driver, locatorValue);
                            break;

                        default:
                            System.out.printf("⚠️ Unknown action '%s'%n", action);
                    }

                } catch (Exception e) {
                    System.out.printf("❌ Error in step '%s': %s%n", action, e.getMessage());
                }
            }

            System.out.println("✅ Test ran successfully.");

        } catch (Exception e) {
            System.out.println("❌ Test failed: " + e.getMessage());
        } finally {
            // driver.quit(); // optionally close browser after test
        }
    }

    public void runGherkinStyleTest(TestCaseDto testCase) {
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            for (String step : testCase.getStepsAsText()) {
                String locatorType = "";

                if (step.startsWith("navigate to")) {
                    driver.get(step.replace("navigate to", "").trim());

                } else if (step.startsWith("enter")) {
                    String[] parts = step.replace("enter", "").trim().split(" into ");
                    String value = parts[0].replace("\"", "").trim();
                    String field = parts[1].replace("\"", "").trim();
                    ElementFinder.findSmart(driver, field, "id").sendKeys(value);

                } else if (step.startsWith("click")) {
                    String target = step.replace("click", "").replace("button", "").trim().replace("\"", "");
                    ElementFinder.findSmart(driver, target, locatorType).click();

                } else if (step.startsWith("keypress")) {
                    String[] parts = step.replace("keypress", "").replace("key", "").trim().split(" in ");
                    String key = parts[0].replace("\"", "").trim();
                    String field = parts[1].replace("\"", "").trim();
                    Keys seleniumKey = getKeyFromString(key);
                    if (seleniumKey != null) ElementFinder.findSmart(driver, field, locatorType).sendKeys(seleniumKey);

                } else if (step.startsWith("assert")) {
                    assertGenericPresence(driver, step);
                }
            }

            System.out.println("✅ Gherkin-style test ran successfully.");
        } catch (Exception e) {
            System.out.println("❌ Gherkin test failed: " + e.getMessage());
        } finally {
            // driver.quit();
        }
    }

    private void assertGenericPresence(WebDriver driver, String stepOrValue) {
        String expected;

        System.out.printf("🧐 Looking for element with title or text='%s'%n", stepOrValue);

        if ((expected = extractValue(stepOrValue, "title")) != null) {
            String actualTitle = driver.getTitle();
            System.out.printf("🧐 Actual page title: '%s'%n", actualTitle);
            if (!actualTitle.contains(expected)) {
                throw new AssertionError("❌ Page title does not contain: " + expected);
            }

        } else if ((expected = extractValue(stepOrValue, "img")) != null) {
            List<WebElement> imgs = driver.findElements(By.tagName("img"));
            String finalExpected = expected;
            boolean found = imgs.stream().anyMatch(img -> {
                String src = img.getAttribute("src");
                return src != null && src.contains(finalExpected);
            });
            if (!found) throw new AssertionError("❌ Image not found in src: " + expected);

        } else if ((expected = extractValue(stepOrValue, "footer")) != null) {
            WebElement footer = driver.findElement(By.tagName("footer"));
            if (!footer.getText().contains(expected)) {
                throw new AssertionError("❌ Footer does not contain: " + expected);
            }

        } else {
            expected = stepOrValue.replace("assert", "").trim().replace("\"", "");
            if (!driver.getPageSource().contains(expected)) {
                throw new AssertionError("❌ Expected text not found in page source: " + expected);
            }
        }

        System.out.println("✅ Assertion passed: " + expected);
    }

    private String extractValue(String input, String key) {
        Pattern pattern = Pattern.compile("\\[" + key + "=([^\\]]+)]");
        Matcher matcher = pattern.matcher(input);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private Keys getKeyFromString(String keyName) {
        try {
            return Keys.valueOf(keyName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
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
}

/*package service;

import dto.StepDto;
import org.openqa.selenium.support.ui.ExpectedConditions;
import util.ElementFinder;
import dto.TestCaseDto;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestCaseService {


    String action;
    String locatorType;
    String locatorValue;
    String value;

    WebDriver driver = new ChromeDriver();
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

    public void runTestCase(TestCaseDto testCase) {

        try {
            driver.get(testCase.getTargetUrl());

            for (StepDto step : testCase.getSteps()) {
                 action = step.getAction().toLowerCase();
                 locatorType = step.getLocatorType();  // ✅ use the correct getter
                 locatorValue = step.getProperty();    // ✅ property is the locator value
                 value = step.getValue();

                try {

                    WebElement element = ElementFinder.findSmart(driver, step.getProperty());


                    switch (action) {
                        case "type":
                            element.clear();
                            element.sendKeys(value);
                            break;
                        case "click":
                            element.click();
                            break;

                        case "should_see":
                            System.out.printf("🧐 Looking for element with title or text='%s'%n", value);
                            break;

                            List<WebElement> elements = driver.findElements(By.xpath(
                                    "//*[text()=\"" + locatorValue + "\" or @title=\"" + locatorValue + "\" or @aria-label=\"" + locatorValue + "\" or @alt=\"" + locatorValue + "\"]"
                            ));

                            boolean found = elements.stream().anyMatch(WebElement::isDisplayed);

                            if (found) {
                                System.out.println("✅ Element with expected content found and visible.");
                            } else {
                                throw new AssertionError("❌ Expected element not found or not visible: " + locatorValue);
                            }
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
        for (StepDto step : testCase.getSteps()) {
            action = step.getAction().toLowerCase();
            locatorType = step.getLocatorType();  // ✅ use the correct getter
            locatorValue = step.getProperty();    // ✅ property is the locator value
            value = step.getValue();
        }
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
                    String field = step.replace("click", "").replace("button", "").trim().replace("\"", "");
                    WebElement button = ElementFinder.findSmart(driver, field);
                    // button = resolveLocator(getlocatortype, getproperty);
                    button.click();

                } else if (step.contains("should_see")) {

                    WebElement element = ElementFinder.findSmart(driver, locatorValue);
                    String expected = value;
                    if (!element.isDisplayed() || !element.getText().contains(expected)) {
                        throw new AssertionError("❌ Expected text not found or not visible: " + expected);
                    }
                    System.out.printf("🧐 Looking for element with title or text='%s'%n", expected);

                    if (!driver.getPageSource().contains(expected)) {
                        throw new AssertionError("Expected text not found: " + expected);
                    }

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
}*/
   /* private By resolveLocator(String locatorType, String property) {
        switch (locatorType.toLowerCase()) {
            case "id":
                return By.id(property);
            case "name":
                return By.name(property);
            case "css":
                return By.cssSelector(property);
            case "xpath":
                return By.xpath(property);
           /* case "alt":
                return By.cssSelector("img[alt='" + property + "']");
            case "alt":
                // XPath for img with exact alt text match
                return By.xpath("//img[@alt='" + property + "']");

            case "tag":
                return By.tagName(property);
            default:
                throw new IllegalArgumentException("❌ Unsupported locator type: " + locatorType);
        }
    }*/
