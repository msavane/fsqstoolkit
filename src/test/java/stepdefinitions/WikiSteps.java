package stepdefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

import static org.junit.Assert.assertTrue;

public class WikiSteps {

    WebDriver driver;

    @Given("I open the Wikipedia main page")
    public void i_open_the_wikipedia_main_page() {
        driver = new ChromeDriver();
        driver.get("https://en.wikipedia.org/wiki/Main_Page");
    }

    @When("I enter {string} into the {string} field")
    public void i_enter_into_the_field(String text, String fieldName) {
        WebElement field = driver.findElement(By.name(fieldName));
        field.clear();
        field.sendKeys(text);
    }

    @When("I press the {string} key in the {string} field")
    public void i_press_the_key_in_the_field(String key, String fieldName) {
        WebElement field = driver.findElement(By.name(fieldName));
        if (key.equalsIgnoreCase("ENTER")) {
            field.sendKeys(Keys.ENTER);
        }
    }

    @Then("I should see the {string} article page")
    public void i_should_see_the_article_page(String articleTitle) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.titleContains(articleTitle));

        String actualTitle = driver.getTitle();
        assertTrue("Expected title to contain '" + articleTitle + "', but was: " + actualTitle,
                actualTitle.toLowerCase().contains(articleTitle.toLowerCase()));
        driver.quit();
    }
}
