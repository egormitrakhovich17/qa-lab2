package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By emailInput = By.name("email");
    private final By passwordInput = By.name("password");
    private final By loginBtn = By.cssSelector("input[value='Login']");
    private final By errorMessage = By.cssSelector(".error_message");
    private final By searchInput = By.name("keyword");
    private final By searchBtn = By.cssSelector("input[value='Search']");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    public void open() {
        driver.get("https://www.sharelane.com/cgi-bin/main.py");
    }

    public void login(String email, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).sendKeys(email);
        driver.findElement(passwordInput).sendKeys(password);
        driver.findElement(loginBtn).click();
    }

    public String getErrorMessage() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public void search(String text) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput)).sendKeys(text);
        driver.findElement(searchBtn).click();
    }
}