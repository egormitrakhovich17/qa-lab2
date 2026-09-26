package tests;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class TheInternetHerokuTest extends BaseTest {

    // 1. Basic Auth
    @Test(description = "1. Проверка Basic Auth")
    public void testBasicAuth() {
        driver.get("https://admin:admin@the-internet.herokuapp.com/basic_auth");
        WebElement successMessage = driver.findElement(By.cssSelector("div.example p"));
        Assert.assertTrue(successMessage.getText().contains("Congratulations! You must have the proper credentials."),
                "Сообщение об успешной аутентификации не найдено!");
    }

    // 2. Dropdown
    @Test(description = "2. Проверка выбора опции из Dropdown")
    public void testDropdown() {
        driver.get(BASE_URL + "/dropdown");
        WebElement dropdownElement = driver.findElement(By.id("dropdown"));
        
        Select select = new Select(dropdownElement);
        select.selectByVisibleText("Option 2");

        WebElement selectedOption = select.getFirstSelectedOption();
        Assert.assertEquals(selectedOption.getText(), "Option 2", "Выбрана неверная опция!");
    }

    // 3. Dynamic Controls
    @Test(description = "3. Проверка Dynamic Controls")
    public void testDynamicControls() {
        driver.get(BASE_URL + "/dynamic_controls");

        WebElement removeBtn = driver.findElement(By.cssSelector("#checkbox-example button"));
        removeBtn.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("checkbox")));
        WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("message")));
        Assert.assertEquals(message.getText(), "It's gone!");

        WebElement enableBtn = driver.findElement(By.cssSelector("#input-example button"));
        enableBtn.click();
        WebElement inputField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#input-example input")));
        inputField.sendKeys("Automation");
        Assert.assertEquals(inputField.getAttribute("value"), "Automation");
    }

    // 4. Exit Intent
    @Test(description = "4. Проверка модального окна Exit Intent")
    public void testExitIntent() {
        driver.get(BASE_URL + "/exit_intent");

        // Эмулируем увод мыши за пределы документа
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
            "var evt = new MouseEvent('mouseleave', {" +
            "  bubbles: true," +
            "  cancelable: true," +
            "  view: window," +
            "  clientY: 0" +
            "});" +
            "document.documentElement.dispatchEvent(evt);"
        );

        WebElement modalTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".modal-title h3")));
        Assert.assertEquals(modalTitle.getText(), "THIS IS A MODAL WINDOW");

        // Закрываем модальное окно через JS-клик для стабильности
        WebElement closeBtn = driver.findElement(By.cssSelector(".modal-footer p"));
        js.executeScript("arguments[0].click();", closeBtn);

        WebElement modal = driver.findElement(By.id("ouibounce-modal"));
        wait.until(driver -> "none".equals(modal.getCssValue("display")) || !modal.isDisplayed());
    }

    // 5. Form Authentication
    @Test(description = "5. Проверка логина: негативный и позитивный сценарии")
    public void testFormAuthentication() {
        driver.get(BASE_URL + "/login");

        // Негативный
        driver.findElement(By.id("username")).sendKeys("tomsmith");
        driver.findElement(By.id("password")).sendKeys("WrongPassword!");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        WebElement flashError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("flash")));
        Assert.assertTrue(flashError.getText().contains("Your password is invalid!"));

        // Позитивный
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys("tomsmith");
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("SuperSecretPassword!");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        WebElement flashSuccess = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("flash")));
        Assert.assertTrue(flashSuccess.getText().contains("You logged into a secure area!"));
    }

    // 6. Frames
    @Test(description = "6. Проверка работы с iframe")
    public void testFrames() {
        driver.get(BASE_URL + "/iframe");

        try {
            driver.findElement(By.cssSelector(".tox-notification__dismiss-button")).click();
        } catch (Exception ignored) {}

        driver.switchTo().frame("mce_0_ifr");
        WebElement editorBody = driver.findElement(By.id("tinymce"));

        // Устанавливаем текст напрямую в contenteditable тело редактора
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].innerHTML = '<p>Hello from automated test!</p>';", editorBody);

        Assert.assertEquals(editorBody.getText().trim(), "Hello from automated test!");
        driver.switchTo().defaultContent();
    }

    // 7. JQuery UI Menus
    @Test(description = "7. Проверка меню JQuery UI с наведением курсора")
    public void testJQueryMenu() {
        driver.get(BASE_URL + "/jqueryui/menu");

        Actions actions = new Actions(driver);
        WebElement enabledItem = driver.findElement(By.id("ui-id-3"));
        actions.moveToElement(enabledItem).perform();

        WebElement downloadsItem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ui-id-4")));
        actions.moveToElement(downloadsItem).perform();

        WebElement pdfItem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ui-id-5")));
        Assert.assertTrue(pdfItem.isDisplayed(), "Пункт 'PDF' не отобразился!");
    }

    // 8. JavaScript Alerts
    @Test(description = "8. Проверка JavaScript Alerts")
    public void testJavaScriptAlerts() {
        driver.get(BASE_URL + "/javascript_alerts");

        // JS Alert
        driver.findElement(By.xpath("//button[text()='Click for JS Alert']")).click();
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        Assert.assertEquals(alert.getText(), "I am a JS Alert");
        alert.accept();
        Assert.assertEquals(driver.findElement(By.id("result")).getText(), "You successfully clicked an alert");

        // JS Confirm
        driver.findElement(By.xpath("//button[text()='Click for JS Confirm']")).click();
        Alert confirm = wait.until(ExpectedConditions.alertIsPresent());
        confirm.dismiss();
        Assert.assertEquals(driver.findElement(By.id("result")).getText(), "You clicked: Cancel");

        // JS Prompt
        driver.findElement(By.xpath("//button[text()='Click for JS Prompt']")).click();
        Alert prompt = wait.until(ExpectedConditions.alertIsPresent());
        prompt.sendKeys("Test message");
        prompt.accept();
        Assert.assertEquals(driver.findElement(By.id("result")).getText(), "You entered: Test message");
    }

    // 9. Multiple Windows
    @Test(description = "9. Проверка открытия нового окна")
    public void testMultipleWindows() {
        driver.get(BASE_URL + "/windows");
        String originalWindow = driver.getWindowHandle();

        driver.findElement(By.linkText("Click Here")).click();
        wait.until(driver -> driver.getWindowHandles().size() == 2);

        List<String> windows = new ArrayList<>(driver.getWindowHandles());
        for (String windowHandle : windows) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        WebElement newWindowHeader = driver.findElement(By.tagName("h3"));
        Assert.assertEquals(newWindowHeader.getText(), "New Window");

        driver.close();
        driver.switchTo().window(originalWindow);
        Assert.assertEquals(driver.getTitle(), "The Internet");
    }
}