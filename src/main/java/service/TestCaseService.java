package service;

import dto.StepDto;
import dto.TestCaseDto;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import util.ElementFinder;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestCaseService {

    public void runTestCase(TestCaseDto testCase) {
        if (testCase == null || testCase.getSteps() == null || testCase.getSteps().isEmpty()) {
            System.out.println("❌ No test case provided or it contains no steps.");
            return;
        }

        boolean isApiTest = testCase.getSteps().stream().anyMatch(step ->
                step.getAction().equalsIgnoreCase("GET") ||
                        step.getAction().equalsIgnoreCase("POST") ||
                        step.getAction().equalsIgnoreCase("ASSERT_BODY")
        );

        if (isApiTest) {
            runRestTestCase(testCase);
            return;
        }

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
            // driver.quit();
        }
    }

    public void runGherkinStyleTest(TestCaseDto testCase) {
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        List<String> locatorType = testCase.getAllLocatorTypes();

        try {
            int i = 0;
            for (String step : testCase.getStepsAsText()) {

                if (step.startsWith("navigate to")) {
                    driver.get(step.replace("navigate to", "").trim());

                } else if (step.startsWith("enter")) {
                    String[] parts = step.replace("enter", "").trim().split(" into ");
                    String value = parts[0].replace("\"", "").trim();
                    String field = parts[1].replace("\"", "").trim();
                    ElementFinder.findSmart(driver, field, locatorType.get(i)).sendKeys(value);

                } else if (step.startsWith("click")) {
                    String target = step.replace("click", "").replace("button", "").trim().replace("\"", "");
                    ElementFinder.findSmart(driver, target, locatorType.get(i)).click();

                } else if (step.startsWith("keypress")) {
                    String[] parts = step.replace("keypress", "").replace("key", "").trim().split(" in ");
                    String key = parts[0].replace("\"", "").trim();
                    String field = parts[1].replace("\"", "").trim();
                    Keys seleniumKey = getKeyFromString(key);
                    if (seleniumKey != null) ElementFinder.findSmart(driver, field, locatorType.get(i)).sendKeys(seleniumKey);

                } else if (step.startsWith("assert")) {
                    String raw = step;
                    String val = raw.replace("assert", "").trim();
                    ElementFinder.findSmart(driver, val, locatorType.get(i));
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

    public void runRestTestCase(TestCaseDto testCase) {
        HttpClient client = HttpClient.newHttpClient();
        String lastResponseBody = "";

        try {
            for (StepDto step : testCase.getSteps()) {
                switch (step.getAction().toUpperCase()) {
                    case "GET":
                        HttpRequest getRequest = HttpRequest.newBuilder()
                                .uri(URI.create(step.getProperty()))
                                .GET()
                                .build();
                        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
                        lastResponseBody = getResponse.body();
                        System.out.println("✅ GET Response: " + lastResponseBody);
                        break;

                    case "POST":
                        HttpRequest postRequest = HttpRequest.newBuilder()
                                .uri(URI.create(step.getProperty()))
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(step.getValue()))
                                .build();
                        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
                        lastResponseBody = postResponse.body();
                        System.out.println("✅ POST Response: " + lastResponseBody);
                        break;

                    case "ASSERT_BODY":
                        if (!lastResponseBody.contains(step.getValue())) {
                            throw new AssertionError("❌ Body does not contain expected text: " + step.getValue());
                        } else {
                            System.out.println("✅ Body contains: " + step.getValue());
                        }
                        break;

                    default:
                        System.out.println("⚠️ Unknown API action: " + step.getAction());
                }
            }

            System.out.println("✅ REST-style test ran successfully.");

        } catch (Exception e) {
            System.out.println("❌ REST test failed: " + e.getMessage());
        }
    }

    public void assertGenericPresence(WebDriver driver, String expectedLocatorValue) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        String titleText = null;
        if (expectedLocatorValue.contains("title=")) {
            int index = expectedLocatorValue.indexOf("title=") + 6;
            titleText = expectedLocatorValue.substring(index).trim();
            if (titleText.endsWith("]")) {
                titleText = titleText.substring(0, titleText.length() - 1).trim();
            }
        } else {
            titleText = expectedLocatorValue;
        }

        By locator = By.xpath("//*[@title='" + titleText + "']");

        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        System.out.println("✅ Assertion passed: " + titleText);
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

        System.out.printf("🧪 Feature:         %s%n", safe(testCase.getFeatureName()));
        System.out.printf("🌐 Target URL:      %s%n%n", safe(testCase.getTargetUrl()));

        System.out.println("🔁 Steps:");
        List<StepDto> steps = testCase.getSteps();
        int i = 1;
        for (StepDto step : steps) {
            System.out.printf("  %d. [%s] using [%s=%s] => %s%n", i++,
                    safe(step.getAction()),
                    safe(step.getLocatorType()),
                    safe(step.getProperty()),
                    safe(step.getValue()));
        }

        System.out.printf("%n🎯 Event Trigger: %s%n", safe(testCase.getEventListener()));
        System.out.println("=================================\n");
    }

    public void saveTestCaseToFile(TestCaseDto testCase, String filename, boolean gherkin) {
        try (FileWriter writer = new FileWriter(filename)) {
            if (gherkin) {
                for (String step : testCase.getStepsAsText()) {
                    writer.write(step + System.lineSeparator());
                }
            } else {
                writer.write("Feature: " + safe(testCase.getFeatureName()) + "\n");
                writer.write("Target URL: " + safe(testCase.getTargetUrl()) + "\n");
                writer.write("Event Trigger: " + safe(testCase.getEventListener()) + "\n\n");

                for (StepDto step : testCase.getSteps()) {
                    writer.write(String.format("Action: %s, Locator Type: %s, Locator Value: %s, Value: %s%n",
                            safe(step.getAction()),
                            safe(step.getLocatorType()),
                            safe(step.getProperty()),
                            safe(step.getValue())));
                }
            }
            System.out.printf("✅ Test case saved to file: %s%n", filename);
        } catch (IOException e) {
            System.out.printf("❌ Failed to save test case to file: %s%n", e.getMessage());
        }
    }

    private String safe(String input) {
        return input != null ? input : "";
    }
}
