// src/main/java/util/ElementFinder.java
package util;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

import java.util.List;

public class ElementFinder {

    public static WebElement findSmart(WebDriver driver, String locatorValue, String locatorType) {
        List<By> strategies = new java.util.ArrayList<>();

        // 🔍 ALT attribute detection
        if (locatorValue.startsWith("alt=")) {
            String altText = locatorValue.substring(4).trim();
            strategies.add(By.xpath("//img[@alt=\"" + altText + "\"]"));

            // 🔍 TITLE attribute detection (new, minimal patch)
        } else if (locatorType.contains("title") || locatorValue.contains("title")) {

            String titleText;
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            if (locatorValue.contains("title=")) {
                // Find where title= starts
                int titleIndex = locatorValue.indexOf("title=") + 6; // after title=

                // Take substring from title= to the end
                String afterTitle = locatorValue.substring(titleIndex).trim();

                // Remove trailing ] if it exists (just one, safe)
                if (afterTitle.endsWith("]")) {
                    afterTitle = afterTitle.substring(0, afterTitle.length() - 1).trim();
                }

                // Now afterTitle should be full title, including nested brackets
                titleText = afterTitle;

                // Wait for element with exact title attribute
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//*[@title='" + titleText + "']")
                ));

                strategies.add(By.xpath("//*[@title='" + titleText + "']"));
            }
            else {
                // If locatorType is explicitly "title", then locatorValue *is* the title
                titleText = locatorValue;
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@title=\"" + titleText + "\"]")));
                strategies.add(By.xpath("//*[@title=\"" + titleText + "\"]"));
            }


            //wait 15 second do nothing


            // 🔍 Default strategies
        } else {
            strategies.addAll(List.of(By.id(locatorValue), By.name(locatorValue), By.cssSelector(locatorValue), By.xpath(locatorValue)));
// Add By.className only if locatorValue does NOT contain spaces
            if (!locatorValue.contains(" ")) {
                strategies.add(By.className(locatorValue));
            }

        }

        for (By by : strategies) {
            try {
                WebElement element = driver.findElement(by);
                if (element != null) return element;
            } catch (NoSuchElementException ignored) {
            }
        }

        throw new NoSuchElementException("❌ Element not found with any strategy for: " + locatorValue);
    }

}
