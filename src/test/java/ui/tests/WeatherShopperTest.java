package ui.tests;

import actions.WeatherShopperActions;
import io.qameta.allure.*;
import org.openqa.selenium.*;
import org.testng.*;
import org.testng.annotations.*;
import setup.DriverFactory;
import utils.RetryAnalyzer;

import java.io.ByteArrayInputStream;

@Epic("Weather Shopper")
@Feature("Shopping functionality")
public class WeatherShopperTest {

    private WebDriver driver;
    private WeatherShopperActions app;

    @Parameters("browser")
    @BeforeMethod
    public void setup(@Optional("chrome") String browser) throws Exception {
        boolean remote = Boolean.parseBoolean(System.getProperty("remote", "false"));
        driver = DriverFactory.createDriver(browser, remote);
        driver.manage().window().maximize();
        app = new WeatherShopperActions(driver);
    }


    @Test(description = "Shop based on temperature", retryAnalyzer = RetryAnalyzer.class)
    @Description("This test buys products based on current temperature")
    @Severity(SeverityLevel.CRITICAL)
    public void shopBasedOnTemperature() {
        app.navigateToHomePage();
        int temp = app.getCurrentTemperature();

        if (temp < 19) { app.clickBuyMoisturizers();
            shopItems("Aloe", "Almond");
        } else if (temp > 34) {
            app.clickBuySunscreens();
            shopItems("SPF-50", "SPF-30");
        } else {
            System.out.println("Temperature moderate → No shopping needed."); }
    }

    private void shopItems(String... keywords) {
        for (String keyword : keywords) {
            app.addCheapestProduct(keyword);
        }
        app.goToCart();
        app.verifyCartContains(keywords);
        app.clickPayWithCard();
        app.fillPaymentDetails();
        app.verifyPaymentResult();
    }

    @AfterMethod(alwaysRun = true)
    public void teardown(ITestResult result) {
        if ((result.getStatus() == ITestResult.FAILURE || result.getStatus() == ITestResult.SKIP) && driver != null) {
            System.out.println("[FAILURE] Capturing screenshot for: " + result.getName());
            Allure.addAttachment("Screenshot on Failure",
                    new ByteArrayInputStream(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES)));
        }
        if (driver != null) driver.quit();
    }
}
