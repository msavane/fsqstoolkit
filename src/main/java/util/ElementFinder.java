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
        } else if (locatorType.startsWith("title") || (locatorValue.startsWith("title"))) {
            String titleText = locatorValue;


            //wait 15 second
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@title=\"" + titleText + "\"]")));


            strategies.add(By.xpath("//*[@title=\"" + titleText + "\"]"));

            // 🔍 Default strategies
        } else {
            strategies.addAll(List.of(
                    By.id(locatorValue),
                    By.name(locatorValue),
                    By.cssSelector(locatorValue),
                    By.xpath(locatorValue)
            ));
// Add By.className only if locatorValue does NOT contain spaces
            if (!locatorValue.contains(" ")) {
                strategies.add(By.className(locatorValue));
            }

        }

        for (By by : strategies) {
            try {
                WebElement element = driver.findElement(by);
                if (element != null) return element;
            } catch (NoSuchElementException ignored) {}
        }

        throw new NoSuchElementException("❌ Element not found with any strategy for: " + locatorValue);
    }

}
