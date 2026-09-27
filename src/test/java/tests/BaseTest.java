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
import java.util.Optional;

public class BaseTest {
    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-notifications");
        // Игнорируем проблемы с SSL старых учебных сайтов:
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--allow-running-insecure-content");
        options.setAcceptInsecureCerts(true);

        findChromiumBinary().ifPresent(options::setBinary);

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private Optional<String> findChromiumBinary() {
        String[] systemPaths = {"/usr/bin/google-chrome", "/usr/bin/chromium", "/usr/bin/chromium-browser"};
        for (String p : systemPaths) {
            if (new File(p).exists()) {
                return Optional.of(p);
            }
        }

        try {
            Path cacheDir = Paths.get(System.getProperty("user.home"), ".cache", "ms-playwright");
            if (Files.exists(cacheDir)) {
                return Files.walk(cacheDir)
                        .filter(p -> p.getFileName().toString().equals("chrome") && Files.isExecutable(p))
                        .map(Path::toString)
                        .findFirst();
            }
        } catch (Exception ignored) {
        }

        return Optional.empty();
    }
}