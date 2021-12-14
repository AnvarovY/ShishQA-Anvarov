import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class FirstAttempts {

    private WebDriver driver;
    private String baseUrl;

    @Before
    public void setUp() throws Exception {
        System.setProperty("webdriver.chrome.driver", "src/test/chromedriver.exe");
        driver = new ChromeDriver();
        baseUrl = "https://sandbox.cardpay.com/MI/cardpayment2.html?orderXml=PE9SREVSIFdBTExFVF9JRD0nODI5OScgT1JERVJfTlVNQkVSPSc0NTgyMTEnIEFNT1VOVD0nMjkxLjg2JyBDVVJSRU5DWT0nRVVSJyAgRU1BSUw9J2N1c3RvbWVyQGV4YW1wbGUuY29tJz4KPEFERFJFU1MgQ09VTlRSWT0nVVNBJyBTVEFURT0nTlknIFpJUD0nMTAwMDEnIENJVFk9J05ZJyBTVFJFRVQ9JzY3NyBTVFJFRVQnIFBIT05FPSc4NzY5OTA5MCcgVFlQRT0nQklMTElORycvPgo8L09SREVSPg==&sha512=998150a2b27484b776a1628bfe7505a9cb430f276dfa35b14315c1c8f03381a90490f6608f0dcff789273e05926cd782e1bb941418a9673f43c47595aa7b8b0d";
        driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);

    }

    //проверка на то что в окне с подтверждением оплаты указаны верные данные
    @Test
    public void testOrderData() throws Exception {
        driver.get(baseUrl);

        String cardNumber = "4000 0000 0000 0002";
        String Last4Numbers = "..." + cardNumber.substring(Math.max(0, cardNumber.length() - 4));
        String totalAmount = driver.findElement(By.id("total-amount")).getText();
        String OrderNumber = driver.findElement(By.id("order-number")).getText();
        String Cardholder = "IVAN IVANOV";

        driver.findElement(By.id("input-card-number")).click();
        driver.findElement(By.id("input-card-number")).clear();
        driver.findElement(By.id("input-card-number")).sendKeys(cardNumber);
        String CardType = "VISA";
        driver.findElement(By.id("input-card-holder")).click();
        driver.findElement(By.id("input-card-holder")).clear();
        driver.findElement(By.id("input-card-holder")).sendKeys(Cardholder);
        driver.findElement(By.id("card-expires-month")).click();
        new Select(driver.findElement(By.id("card-expires-month"))).selectByVisibleText("03");
        driver.findElement(By.id("card-expires-year")).click();
        new Select(driver.findElement(By.id("card-expires-year"))).selectByVisibleText("2023");
        driver.findElement(By.id("input-card-cvc")).click();
        driver.findElement(By.id("input-card-cvc")).clear();
        driver.findElement(By.id("input-card-cvc")).sendKeys("123");
        driver.findElement(By.id("action-submit")).click();
        driver.findElement(By.id("success")).click();

        Assert.assertEquals(OrderNumber, driver.findElement(By.xpath("//*[@id=\"payment-item-ordernumber\"]/div[2]")).getText());
        Assert.assertEquals(totalAmount, driver.findElement(By.xpath("//*[@id=\"payment-item-total-amount\"]")).getText());
        Assert.assertEquals(Last4Numbers, driver.findElement(By.xpath("//*[@id=\"payment-item-cardnumber\"]/div[2]")).getText());
        Assert.assertEquals(Cardholder, driver.findElement(By.xpath("//*[@id=\"payment-item-cardholder\"]/div[2]")).getText());
        Assert.assertEquals(CardType, driver.findElement(By.xpath("//*[@id=\"payment-item-cardtype\"]/div[2]")).getText());

    }
// проверка на то что необходимые данные не введены
    @Test
    public void emptyFieldsTest() throws Exception {
        driver.get(baseUrl);

        driver.findElement(By.id("action-submit")).click();

        Assert.assertEquals("Card number is required", driver.findElement(By.xpath("//*[@id=\"card-number-field\"]/div/label")).getText());
        Assert.assertEquals("Cardholder name is required", driver.findElement(By.xpath("//*[@id=\"card-holder-field\"]/div/label")).getText());
        Assert.assertEquals("Expiration Date is required", driver.findElement(By.xpath("//*[@id=\"card-expires-field\"]/div/label")).getText());
        Assert.assertEquals("CVV2/CVC2/CAV2 is required", driver.findElement(By.xpath("//*[@id=\"card-cvc-field\"]/div/label")).getText());

    }

    // проверка поля номера карты на некорректные данные
    @Test
    public void testForCardNumber() throws Exception {
        driver.get(baseUrl);

        String[] cardNumberNonDigit  = {"dafgsrga", "!~er5{}", "<div>"};
        String[] cardNumberNotValid  = {"12345", "400000000000002", "4000000000000000", "40000000000000020"};

        WebElement inputCardNumber = driver.findElement(By.id("input-card-number"));
        WebElement actionSubmit = driver.findElement(By.id("action-submit"));

        for (String s: cardNumberNonDigit) {
            inputCardNumber.clear();
            inputCardNumber.sendKeys(s);
            Assert.assertNotEquals(s, inputCardNumber.getText());
        }

        for (String s: cardNumberNotValid) {
            inputCardNumber.clear();
            inputCardNumber.sendKeys(s);
            actionSubmit.click();
            Assert.assertEquals("Card number is not valid", driver.findElement(By.xpath("//*[@id=\"card-number-field\"]/div/label")).getText());
        }
    }

    // проверка поля ввода держателя карты
    @Test
    public void testForCardHolder() throws Exception {
        driver.get(baseUrl);

        WebElement inputCardHolder = driver.findElement(By.id("input-card-holder"));
        WebElement actionSubmit = driver.findElement(By.id("action-submit"));

        //неккоректные данные
        String[] HolderNonValidData = {"214456", "dfgs!~{}&?", "<div>", "'ivan'"};
        //ограничение на имя от 4 до 50 символов
        String[] outOfLength = {"IVA", "IVANOVIVANIVANOVIVANIVANOVIVANIVANOVIVANIVANOVIVANI"};

        for (String s : HolderNonValidData) {
            inputCardHolder.clear();
            inputCardHolder.sendKeys(s);
            actionSubmit.click();
            Assert.assertEquals("Cardholder name is not valid", driver.findElement(By.xpath("//*[@id=\"card-holder-field\"]/div/label")).getText());
        }

        for (String s : outOfLength) {
            inputCardHolder.clear();
            inputCardHolder.sendKeys(s);
            actionSubmit.click();
            Assert.assertEquals("Allowed from 4 to 50 characters", driver.findElement(By.xpath("//*[@id=\"card-holder-field\"]/div/label")).getText());
        }
    }

    // проверка выбора срока действия карты
    @Test
    public void testForDateExpires() throws Exception {
        driver.get(baseUrl);

        WebElement cardExpiresMonth = driver.findElement(By.id("card-expires-month"));
        WebElement cardExpiresYear = driver.findElement(By.id("card-expires-year"));
        WebElement actionSubmit = driver.findElement(By.id("action-submit"));

        //текущая дата (12.2021)
        cardExpiresMonth.click();
        new Select(cardExpiresMonth).selectByVisibleText("12");
        cardExpiresYear.click();
        new Select(cardExpiresYear).selectByVisibleText("2021");
        actionSubmit.click();
        //.assertEquals("none", driver.findElement(By.xpath("//*[@id=\"card-expires-field\"]/div/label")).getCssValue("display")); не видит css атрибут у скрытого элемента
        Assert.assertEquals("valid", driver.findElement(By.xpath("//*[@id=\"card-expires-year\"]")).getAttribute("class"));

        //истекший срок действия (01.2021)
        cardExpiresMonth.click();
        new Select(cardExpiresMonth).selectByVisibleText("01");
        cardExpiresYear.click();
        new Select(cardExpiresYear).selectByVisibleText("2021");
        actionSubmit.click();
        Assert.assertEquals("Invalid date", driver.findElement(By.xpath("//*[@id=\"card-expires-field\"]/div/label")).getText());
    }

    @Test
    public void testCVV() throws Exception {
        driver.get(baseUrl);

        WebElement inputCardCvc = driver.findElement(By.id("input-card-cvc"));
        WebElement actionSubmit = driver.findElement(By.id("action-submit"));

        String[] nonValidCVV = {"awd", "!~6", "3-2", "1*4", "<1>"};
        String outOfLength1 =  "12", outOfLength2 = "1234";

        // Ввод неккоректных данных
        for (String s : nonValidCVV) {
            inputCardCvc.clear();
            inputCardCvc.sendKeys(s);
            Assert.assertNotEquals(s, inputCardCvc.getAttribute("value"));
        }

        // Ввод короткого CVV
        inputCardCvc.clear();
        inputCardCvc.sendKeys(outOfLength1);
        actionSubmit.click();
        Assert.assertEquals("CVV2/CVC2/CAV2 is not valid", driver.findElement(By.xpath("//*[@id=\"card-cvc-field\"]/div/label")).getText());

        // Ввод более 3 символов
        inputCardCvc.clear();
        inputCardCvc.sendKeys(outOfLength2);
        actionSubmit.click();
        Assert.assertNotEquals(outOfLength2.length(), inputCardCvc.getText().length());
    }

    @Test
    public void testCVV_hint() throws IOException {
        driver.get(baseUrl);

        Actions action = new Actions(driver);
        action.moveToElement(driver.findElement(By.id("cvc-hint-toggle"))).build().perform();

        Screenshot scr = new AShot().takeScreenshot(driver);
        ImageIO.write(scr.getImage(), "png", new File(".\\src\\cvvHint.png"));
    }

        @After
    public void tearDown() throws Exception {
        driver.quit();
    }
}
