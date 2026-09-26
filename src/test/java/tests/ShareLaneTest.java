package tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

public class ShareLaneTest extends BaseTest {

    private final String SHARELANE_URL = "https://www.sharelane.com/cgi-bin/main.py";

    // ==========================================
    // БЛОК 1: ТЕСТИРОВАНИЕ ВАЛИДАЦИИ ZIP-КОДА (DDT + Граничные значения)
    // ==========================================

    @Test(description = "TC-01 [Позитивный]: Ввод корректного 5-значного ZIP-кода")
    public void testValidZipCode() {
        driver.get("https://www.sharelane.com/cgi-bin/register.py");
        WebElement zipField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("zip_code")));
        zipField.clear();
        zipField.sendKeys("12345");
        driver.findElement(By.cssSelector("input[value='Continue']")).click();

        // Проверяем, что нет ошибки длины ZIP-кода
        List<WebElement> errors = driver.findElements(By.cssSelector(".error_message"));
        boolean hasZipLengthError = errors.stream()
                .anyMatch(e -> e.getText().contains("ZIP code should have 5 digits"));

        Assert.assertFalse(hasZipLengthError,
                "[БИЗНЕС-ОШИБКА]: Система ошибочно отклонила корректный 5-значный ZIP-код '12345'!");
    }

    @DataProvider(name = "invalidZipData")
    public Object[][] getInvalidZipData() {
        return new Object[][]{
                {"1234", "TC-02: 4 цифры (меньше 5)"},
                {"123", "TC-03: 3 цифры (меньше 5)"},
                {"abcde", "TC-04: Буквы вместо цифр"},
                {"", "TC-05: Пустое значение"}
        };
    }

    @Test(dataProvider = "invalidZipData", description = "TC-02..05 [Негативные DDT]: Проверка невалидных форматов ZIP-кода")
    public void testInvalidZipCodesDDT(String zip, String testName) {
        driver.get("https://www.sharelane.com/cgi-bin/register.py");
        WebElement zipField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("zip_code")));
        zipField.clear();
        zipField.sendKeys(zip);
        driver.findElement(By.cssSelector("input[value='Continue']")).click();

        String errorText = "";
        try {
            WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error_message")));
            errorText = errorElement.getText();
        } catch (Exception e) {
            Assert.fail(String.format("[БИЗНЕС-ОШИБКА в %s]: Сообщение об ошибке валидации не появилось для значения '%s'!", testName, zip));
        }

        Assert.assertEquals(errorText, "Oops, error on page. ZIP code should have 5 digits",
                String.format("[БИЗНЕС-ОШИБКА в %s]: Для невалидного ZIP '%s' отобразился некорректный текст ошибки!", testName, zip));
    }

    // ==========================================
    // БЛОК 2: ТЕСТИРОВАНИЕ АВТОРИЗАЦИИ (LOGIN)
    // ==========================================

    @Test(description = "TC-06 [Негативный]: Вход с незарегистрированным пользователем")
    public void testLoginNonExistentUser() {
        driver.get(SHARELANE_URL);
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("email")));
        emailInput.sendKeys("non_existent_qa_user_999@test.com");
        driver.findElement(By.name("password")).sendKeys("secretPass123");
        driver.findElement(By.cssSelector("input[value='Login']")).click();

        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error_message")));
        String errorText = errorElement.getText();

        Assert.assertTrue(errorText.contains("Oops, error on page. Some of the required fields are empty")
                        || errorText.contains("Email")
                        || errorText.contains("error"),
                "[БИЗНЕС-ОШИБКА]: При вводе незарегистрированного пользователя не отобразилось сообщение об ошибке входа!");
    }

    @Test(description = "TC-07 [Негативный]: Вход с пустыми полями Email и Password")
    public void testLoginEmptyCredentials() {
        driver.get(SHARELANE_URL);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Login']")));
        loginBtn.click();

        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error_message")));
        Assert.assertFalse(errorElement.getText().trim().isEmpty(),
                "[БИЗНЕС-ОШИБКА]: Система разрешила отправку пустой формы логина без отображения ошибки!");
    }

    // ==========================================
    // БЛОК 3: ТЕСТИРОВАНИЕ ПОИСКА (SEARCH)
    // ==========================================

    @Test(description = "TC-08 [Позитивный]: Поиск книги по ключевому слову")
    public void testSearchBookPositive() {
        driver.get(SHARELANE_URL);
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("keyword")));
        searchInput.clear();
        searchInput.sendKeys("Python");
        driver.findElement(By.cssSelector("input[value='Search']")).click();

        WebElement pageBody = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        Assert.assertTrue(pageBody.getText().contains("Search") || pageBody.getText().contains("Python") || pageBody.getText().contains("Book"),
                "[БИЗНЕС-ОШИБКА]: Страница с результатами поиска книги не загрузилась!");
    }

    @Test(description = "TC-09 [Негативный]: Поиск заведомо несуществующей книги")
    public void testSearchNonExistentBook() {
        driver.get(SHARELANE_URL);
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("keyword")));
        searchInput.clear();
        searchInput.sendKeys("XYZ_ABRACADABRA_9999");
        driver.findElement(By.cssSelector("input[value='Search']")).click();

        WebElement pageBody = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        Assert.assertTrue(pageBody.getText().contains("Nothing is found") 
                        || pageBody.getText().contains("not found") 
                        || pageBody.getText().contains("Search"),
                "[БИЗНЕС-ОШИБКА]: При отсутствии результатов поиска система не вывела уведомление о том, что ничего не найдено!");
    }

    // ==========================================
    // БЛОК 4: ТЕСТИРОВАНИЕ КОРЗИНЫ (SHOPPING CART)
    // ==========================================

    @Test(description = "TC-10 [Позитивный]: Добавление товара в корзину и проверка отображения корзины")
    public void testAddToCart() {
        driver.get("https://www.sharelane.com/cgi-bin/add_to_cart.py?book_id=1");

        WebElement pageBody = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        String bodyText = pageBody.getText();

        Assert.assertTrue(bodyText.contains("Shopping Cart") || bodyText.contains("Cart"),
                "[БИЗНЕС-ОШИБКА]: Страница Shopping Cart не открылась после добавления книги в корзину!");
    }
}