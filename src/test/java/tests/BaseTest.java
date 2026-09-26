package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.time.Duration;

public abstract class BaseTest {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected final String BASE_URL = "https://the-internet.herokuapp.com";

    @BeforeMethod
    public void setUp() {
        ChromeOptions options = new ChromeOptions();

        File codespaceBrowser = new File("/home/codespace/.cache/ms-playwright/chromium-1243/chrome-linux64/chrome");
        if (codespaceBrowser.exists()) {
            options.setBinary(codespaceBrowser.getAbsolutePath());
            WebDriverManager.chromedriver().browserVersion("153").setup();
        } else {
            WebDriverManager.chromedriver().setup();
        }

        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-notifications");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}