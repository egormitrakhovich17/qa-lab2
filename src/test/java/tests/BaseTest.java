package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;

public class BaseTest {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected final String BASE_URL = "https://the-internet.herokuapp.com";

    @BeforeMethod
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-notifications");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--allow-running-insecure-content");
        // Маскировка под обычный десктопный браузер для учебных CGI-сайтов:
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        options.setAcceptInsecureCerts(true);

        Optional<String> browserPath = findWorkingChromium();
        if (browserPath.isPresent()) {
            options.setBinary(browserPath.get());
            if (browserPath.get().contains("ms-playwright")) {
                WebDriverManager.chromedriver().browserVersion("153").setup();
            } else {
                WebDriverManager.chromedriver().setup();
            }
        } else {
            WebDriverManager.chromedriver().setup();
        }

        driver = new ChromeDriver(options);
        // Таймаут поиска элементов сокращен до 3 секунд для мгновенного перехвата бизнес-ошибок
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
        wait = new WebDriverWait(driver, Duration.ofSeconds(3));
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private Optional<String> findWorkingChromium() {
        try {
            Path cacheDir = Paths.get(System.getProperty("user.home"), ".cache", "ms-playwright");
            if (Files.exists(cacheDir)) {
                Optional<String> playwrightChrome = Files.walk(cacheDir)
                        .filter(p -> p.getFileName().toString().equals("chrome") && Files.isExecutable(p))
                        .map(Path::toString)
                        .findFirst();
                if (playwrightChrome.isPresent()) {
                    return playwrightChrome;
                }
            }
        } catch (Exception ignored) {
        }

        String[] realBinaries = {"/usr/bin/google-chrome", "/opt/google/chrome/chrome"};
        for (String p : realBinaries) {
            if (new File(p).exists()) {
                return Optional.of(p);
            }
        }

        return Optional.empty();
    }
}