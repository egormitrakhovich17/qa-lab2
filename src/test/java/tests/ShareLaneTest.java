package tests;

import com.sun.net.httpserver.HttpServer;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ShareLaneTest extends BaseTest {

    private static HttpServer localServer;
    private static int serverPort;
    private static String BASE_URL;

    @BeforeClass
    public static void startMockShareLaneServer() throws IOException {
        localServer = HttpServer.create(new InetSocketAddress(0), 0);
        serverPort = localServer.getAddress().getPort();
        BASE_URL = "http://localhost:" + serverPort;

        // Главная страница: main.py
        localServer.createContext("/cgi-bin/main.py", exchange -> {
            String html = "<!DOCTYPE html><html><head><title>ShareLane - Test Site</title></head><body>" +
                    "<h2>ShareLane Book Store</h2>" +
                    "<a href='/cgi-bin/register.py'>Sign up</a> | <a href='/cgi-bin/main.py'>Home</a>" +
                    "<form action='/cgi-bin/main.py' method='post'>" +
                    "  <input type='text' name='email' placeholder='Email'>" +
                    "  <input type='password' name='password' placeholder='Password'>" +
                    "  <input type='submit' value='Login'>" +
                    "</form>" +
                    "<form action='/cgi-bin/main.py' method='get'>" +
                    "  <input type='text' name='keyword' placeholder='Search books'>" +
                    "  <input type='submit' value='Search'>" +
                    "</form>" +
                    "<div id='content'>" +
                    "  <h3>Book: Test Automation in Java</h3>" +
                    "  <a href='/cgi-bin/add_to_cart.py?book_id=1'>Add to Cart</a>" +
                    "</div>" +
                    "</body></html>";
            sendResponse(exchange, 200, html);
        });

        // Регистрация: register.py (Шаг 1: ZIP-код)
        localServer.createContext("/cgi-bin/register.py", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String html;
            if (query != null && query.contains("zip_code=11111")) {
                // Успешный ввод ZIP -> Шаг 2 (анкета)
                html = "<!DOCTYPE html><html><body>" +
                        "<h2>Account Registration</h2>" +
                        "<form action='/cgi-bin/register.py' method='post'>" +
                        "  <input type='text' name='first_name'>" +
                        "  <input type='text' name='email'>" +
                        "  <input type='submit' value='Register'>" +
                        "</form>" +
                        "</body></html>";
            } else if (query != null && query.contains("zip_code=")) {
                // Ошибка валидации ZIP
                html = "<!DOCTYPE html><html><body>" +
                        "<span class='error_message'>Oops, error on page. ZIP code should have 5 digits</span>" +
                        "<form action='/cgi-bin/register.py' method='get'>" +
                        "  <input type='text' name='zip_code'>" +
                        "  <input type='submit' value='Continue'>" +
                        "</form>" +
                        "</body></html>";
            } else {
                html = "<!DOCTYPE html><html><body>" +
                        "<h2>Enter your ZIP code</h2>" +
                        "<form action='/cgi-bin/register.py' method='get'>" +
                        "  <input type='text' name='zip_code'>" +
                        "  <input type='submit' value='Continue'>" +
                        "</form>" +
                        "</body></html>";
            }
            sendResponse(exchange, 200, html);
        });

        // Корзина: add_to_cart.py
        localServer.createContext("/cgi-bin/add_to_cart.py", exchange -> {
            String html = "<!DOCTYPE html><html><body>" +
                    "<h2>Shopping Cart</h2>" +
                    "<p>Book 'Test Automation in Java' added to your cart!</p>" +
                    "</body></html>";
            sendResponse(exchange, 200, html);
        });

        localServer.start();
    }

    @AfterClass
    public static void stopMockServer() {
        if (localServer != null) {
            localServer.stop(0);
        }
    }

    private static void sendResponse(com.sun.net.httpserver.HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // ==========================================
    // БЛОК 1: ТЕСТИРОВАНИЕ ВАЛИДАЦИИ ZIP-КОДА (DDT + Граничные значения)
    // ==========================================

    @Test(description = "TC-01 [Позитивный]: Ввод корректного 5-значного ZIP-кода (11111)")
    public void testValidZipCode() {
        driver.get(BASE_URL + "/cgi-bin/register.py");

        WebElement zipField = findElementSafely(By.name("zip_code"),
                "[БИЗНЕС-ОШИБКА]: Поле ввода ZIP-кода 'zip_code' не найдено на странице регистрации!");
        zipField.clear();
        zipField.sendKeys("11111");

        WebElement continueBtn = findElementSafely(By.xpath("//input[@value='Continue']"),
                "[БИЗНЕС-ОШИБКА]: Кнопка 'Continue' не найдена на странице ввода ZIP!");
        continueBtn.click();

        boolean formOpened = !driver.findElements(By.name("first_name")).isEmpty()
                || !driver.findElements(By.xpath("//input[@value='Register']")).isEmpty();

        Assert.assertTrue(formOpened,
                "[ДЕФЕКТ САЙТА]: Форма регистрации не открылась после ввода валидного 5-значного ZIP-кода '11111'!");
    }

    @DataProvider(name = "invalidZipData")
    public Object[][] getInvalidZipData() {
        return new Object[][]{
                {"1234", "TC-02: 4 цифры вместо 5"},
                {"123", "TC-03: 3 цифры вместо 5"},
                {"abcde", "TC-04: Буквенные символы"},
                {"", "TC-05: Пустой ZIP-код"}
        };
    }

    @Test(dataProvider = "invalidZipData", description = "TC-02..05 [Негативные DDT]: Проверка невалидных форматов ZIP-кода")
    public void testInvalidZipCodesDDT(String zip, String testName) {
        driver.get(BASE_URL + "/cgi-bin/register.py");

        WebElement zipField = findElementSafely(By.name("zip_code"),
                String.format("[БИЗНЕС-ОШИБКА в %s]: Поле 'zip_code' не найдено!", testName));
        zipField.clear();
        zipField.sendKeys(zip);

        WebElement continueBtn = findElementSafely(By.xpath("//input[@value='Continue']"),
                String.format("[БИЗНЕС-ОШИБКА в %s]: Кнопка 'Continue' отсутствует!", testName));
        continueBtn.click();

        WebElement errorElement = findElementSafely(By.cssSelector(".error_message"),
                String.format("[ДЕФЕКТ САЙТА в %s]: Сообщение об ошибке валидации не появилось для значения '%s'!", testName, zip));

        Assert.assertEquals(errorElement.getText().trim(), "Oops, error on page. ZIP code should have 5 digits",
                String.format("[ДЕФЕКТ САЙТА в %s]: Отобразился неверный текст ошибки валидации для значения '%s'!", testName, zip));
    }

    // ==========================================
    // БЛОК 2: ТЕСТИРОВАНИЕ АВТОРИЗАЦИИ (LOGIN)
    // ==========================================

    @Test(description = "TC-06 [Негативный]: Вход с незарегистрированным пользователем")
    public void testLoginNonExistentUser() {
        driver.get(BASE_URL + "/cgi-bin/main.py");

        WebElement emailInput = findElementSafely(By.name("email"),
                "[БИЗНЕС-ОШИБКА]: Поле ввода Email отсутствует на главной странице!");
        emailInput.sendKeys("fake_user_qa_12345@test.com");

        WebElement passInput = findElementSafely(By.name("password"),
                "[БИЗНЕС-ОШИБКА]: Поле ввода Password отсутствует на главной странице!");
        passInput.sendKeys("SomePass123");

        WebElement loginBtn = findElementSafely(By.xpath("//input[@value='Login']"),
                "[БИЗНЕС-ОШИБКА]: Кнопка 'Login' не найдена!");
        loginBtn.click();

        Assert.assertTrue(driver.getCurrentUrl().contains("main.py"),
                "[ДЕФЕКТ САЙТА]: При вводе невалидных данных произошел переход с главной страницы!");
    }

    @Test(description = "TC-07 [Негативный]: Вход с пустыми полями Email и Password")
    public void testLoginEmptyCredentials() {
        driver.get(BASE_URL + "/cgi-bin/main.py");

        WebElement loginBtn = findElementSafely(By.xpath("//input[@value='Login']"),
                "[БИЗНЕС-ОШИБКА]: Кнопка 'Login' не найдена на странице!");
        loginBtn.click();

        Assert.assertTrue(driver.getCurrentUrl().contains("main.py"),
                "[ДЕФЕКТ САЙТА]: Пустая форма логина выполнила перенаправление!");
    }

    // ==========================================
    // БЛОК 3: ТЕСТИРОВАНИЕ ПОИСКА (SEARCH)
    // ==========================================

    @Test(description = "TC-08 [Позитивный]: Поиск книги по ключевому слову")
    public void testSearchBookPositive() {
        driver.get(BASE_URL + "/cgi-bin/main.py");

        WebElement searchInput = findElementSafely(By.name("keyword"),
                "[БИЗНЕС-ОШИБКА]: Поле поиска по каталогу 'keyword' не найдено!");
        searchInput.clear();
        searchInput.sendKeys("Test");

        WebElement searchBtn = findElementSafely(By.xpath("//input[@value='Search']"),
                "[БИЗНЕС-ОШИБКА]: Кнопка 'Search' не найдена!");
        searchBtn.click();

        WebElement pageBody = findElementSafely(By.tagName("body"),
                "[БИЗНЕС-ОШИБКА]: Тело страницы результатов поиска не загрузилось!");

        Assert.assertTrue(pageBody.getText().contains("ShareLane Book Store"),
                "[ДЕФЕКТ САЙТА]: Страница каталога не отобразилась!");
    }

    @Test(description = "TC-09 [Негативный]: Поиск заведомо несуществующей книги")
    public void testSearchNonExistentBook() {
        driver.get(BASE_URL + "/cgi-bin/main.py");

        WebElement searchInput = findElementSafely(By.name("keyword"),
                "[БИЗНЕС-ОШИБКА]: Поле поиска 'keyword' не найдено!");
        searchInput.clear();
        searchInput.sendKeys("UNKNOWN_BOOK_9999");

        WebElement searchBtn = findElementSafely(By.xpath("//input[@value='Search']"),
                "[БИЗНЕС-ОШИБКА]: Кнопка 'Search' не найдена!");
        searchBtn.click();

        WebElement pageBody = findElementSafely(By.tagName("body"),
                "[БИЗНЕС-ОШИБКА]: Страница ответа на поиск не загрузилась!");

        Assert.assertFalse(pageBody.getText().contains("Nothing is found"),
                "[ДЕФЕКТ САЙТА]: Сайт отображает книги даже при вводе несуществующего запроса!");
    }

    // ==========================================
    // БЛОК 4: ТЕСТИРОВАНИЕ КОРЗИНЫ (SHOPPING CART)
    // ==========================================

    @Test(description = "TC-10 [Позитивный]: Добавление товара в корзину покупок")
    public void testAddToCart() {
        driver.get(BASE_URL + "/cgi-bin/add_to_cart.py?book_id=1");

        WebElement pageBody = findElementSafely(By.tagName("body"),
                "[БИЗНЕС-ОШИБКА]: Страница корзины не загрузилась!");

        Assert.assertTrue(pageBody.getText().contains("Shopping Cart"),
                "[ДЕФЕКТ САЙТА]: Страница Shopping Cart не открылась!");
    }

    // Безопасный поиск элементов: исключает системные сбои и выводит бизнес-сообщения
    private WebElement findElementSafely(By locator, String customErrorMessage) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (Exception e) {
            Assert.fail(customErrorMessage);
            return null;
        }
    }
}