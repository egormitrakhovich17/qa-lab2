package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SignUpPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    // Шаг 1: ZIP-код
    private final By zipInput = By.name("zip_code");
    private final By continueBtn = By.cssSelector("input[value='Continue']");
    private final By errorMessage = By.cssSelector(".error_message");

    // Шаг 2: Форма регистрации
    private final By firstNameInput = By.name("first_name");
    private final By lastNameInput = By.name("last_name");
    private final By emailInput = By.name("email");
    private final By passwordInput = By.name("password");
    private final By confirmPasswordInput = By.name("confirm_password");
    private final By registerBtn = By.cssSelector("input[value='Register']");
    private final By confirmationMessage = By.cssSelector(".confirmation_message");

    public SignUpPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    public void open() {
        driver.get("https://www.sharelane.com/cgi-bin/register.py");
    }

    public void enterZipAndContinue(String zip) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(zipInput));
        input.clear();
        input.sendKeys(zip);
        driver.findElement(continueBtn).click();
    }

    public boolean isRegisterFormDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(registerBtn)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessage() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean openRegistrationFormWithValidZip() {
        // Пробуем известные рабочие индексы полигона ShareLane
        String[] validZips = {"30000", "11111", "90210", "12345"};
        for (String zip : validZips) {
            open();
            enterZipAndContinue(zip);
            if (isRegisterFormDisplayed()) {
                return true;
            }
        }
        return isRegisterFormDisplayed();
    }

    public boolean fillRegistrationForm(String first, String last, String email, String pass, String confirmPass) {
        try {
            if (!isRegisterFormDisplayed()) {
                boolean opened = openRegistrationFormWithValidZip();
                if (!opened) return false;
            }

            WebElement firstField = wait.until(ExpectedConditions.visibilityOfElementLocated(firstNameInput));
            firstField.clear();
            firstField.sendKeys(first);

            WebElement lastField = driver.findElement(lastNameInput);
            lastField.clear();
            lastField.sendKeys(last);

            WebElement emailField = driver.findElement(emailInput);
            emailField.clear();
            emailField.sendKeys(email);

            WebElement passField = driver.findElement(passwordInput);
            passField.clear();
            passField.sendKeys(pass);

            WebElement confirmField = driver.findElement(confirmPasswordInput);
            confirmField.clear();
            confirmField.sendKeys(confirmPass);

            driver.findElement(registerBtn).click();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getConfirmationMessage() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(confirmationMessage)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }
}