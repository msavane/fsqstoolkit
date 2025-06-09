// src/main/java/util/ElementFinder.java
package util;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ElementFinder {

    public static WebElement findSmart(WebDriver driver, String locatorValue) {
        List<By> strategies = List.of(
                By.id(locatorValue),
                By.name(locatorValue),
                By.cssSelector(locatorValue),
                By.xpath(locatorValue),
                By.className(locatorValue)
        );

        for (By by : strategies) {
            try {
                WebElement element = driver.findElement(by);
                if (element != null) {
                    return element;
                }
            } catch (NoSuchElementException ignored) {}
        }

        throw new NoSuchElementException("❌ Element not found with any strategy for: " + locatorValue);
    }
}
