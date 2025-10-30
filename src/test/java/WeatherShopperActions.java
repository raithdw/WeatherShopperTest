import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;

import java.time.Duration;
import java.util.List;

import static java.lang.Thread.sleep;

public class WeatherShopperActions {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public WeatherShopperActions(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    }

    @Step("Navigate to Weather Shopper home page")
    public void navigateToHomePage() {
        logStep("Navigating to home page");
        driver.get("https://weathershopper.pythonanywhere.com/");
        wait.until(ExpectedConditions.titleContains("Current Temperature"));
    }

    @Step("Get current temperature")
    public int getCurrentTemperature() {
        String tempText = driver.findElement(By.id("temperature")).getText().replaceAll("[^0-9]", "");
        logStep("Current temperature is " + tempText + "°C");
        return Integer.parseInt(tempText);
    }

    @Step("Click Buy Moisturizers")
    public void clickBuyMoisturizers() {
        logStep("Clicking 'Buy moisturizers'");
        clickButton("//button[contains(text(),'Buy moisturizers')]", "moisturizer");
    }

    @Step("Click Buy Sunscreens")
    public void clickBuySunscreens() {
        logStep("Clicking 'Buy sunscreens'");
        clickButton("//button[contains(text(),'Buy sunscreens')]", "sunscreen");
    }

    private void clickButton(String xpath, String expectedUrlFragment) {
        driver.findElement(By.xpath(xpath)).click();
        wait.until(ExpectedConditions.urlContains(expectedUrlFragment));
    }

    @Step("Verify cart contains all expected items")
    public void verifyCartContains(String[] keywords) {
        logStep("Verifying cart items");
        List<WebElement> cartItems = driver.findElements(By.cssSelector("table tbody tr td:first-child"));
        Assert.assertEquals(cartItems.size(), keywords.length, "Unexpected number of items in cart!");

        for (String keyword : keywords) {
            boolean found = cartItems.stream()
                    .anyMatch(item -> item.getText().toLowerCase().contains(keyword.toLowerCase()));
            Assert.assertTrue(found, "Expected item containing keyword: " + keyword);
        }

        logStep("Cart contains all expected items");

//      Uncomment the line below to Force a failure for testing screenshot attachment inside allure report teardown :)
//        Assert.fail("Forcing failure to test Allure screenshot attachment");
    }

    @Step("Verify payment result")
    public void verifyPaymentResult() {
        logStep("Verifying payment result");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            // Wait for either success or error message
            WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h2[contains(text(),'PAYMENT SUCCESS')]")
            ));
            // Assert that payment was successful
            Assert.assertEquals(successMessage.getText().trim(), "PAYMENT SUCCESS", "Payment success message mismatch!");
            logStep("Payment status: " + successMessage.getText());

        } catch (TimeoutException e) {
            // Possibly the 5% error case
            try {
                WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//h2[contains(text(),'PAYMENT FAILED')]")));
                logStep("Stripe payment failed (random 5% error): " + errorMessage.getText());
                logStep("Retrying payment once...");
                logStep("Going back to cart to retry payment...");
                driver.navigate().back(); // or use your goToCart() method
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table tbody tr")));
                // Retry payment
                clickPayWithCard();
                fillPaymentDetails();

                // Check result again
                verifyPaymentResult();

            } catch (NoSuchElementException ex) {
                Assert.fail("Payment result not found and no error displayed!");
            }
        }
    }

    @Step("Add cheapest product for keyword: {keyword}")
    public void addCheapestProduct(String keyword) {
        List<WebElement> products = driver.findElements(By.cssSelector(".text-center.col-4"));
        int minPrice = Integer.MAX_VALUE;
        WebElement cheapestButton = null;

        for (WebElement product : products) {
            String name = product.findElement(By.tagName("p")).getText();
            if (name.toLowerCase().contains(keyword.toLowerCase())) {
                int price = Integer.parseInt(
                        product.findElements(By.tagName("p")).get(1)
                                .getText().replaceAll("[^0-9]", "")
                );
                if (price < minPrice) {
                    minPrice = price;
                    cheapestButton = product.findElement(By.tagName("button"));
                }
            }
        }

        if (cheapestButton == null)
            throw new NoSuchElementException("No product found for keyword: " + keyword);

        Assert.assertNotNull(cheapestButton, "No product found for keyword: " + keyword);
        logStep("Adding cheapest product for " + keyword + " (Rs." + minPrice + ")");

        // Scroll into view and click safely
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", cheapestButton);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.elementToBeClickable(cheapestButton));

        try {
            cheapestButton.click();
        } catch (ElementClickInterceptedException e) {
            logStep("Element click intercepted, retrying after short wait...");
            // Wait and retry via JavaScript if still blocked
            try {
                sleep(500);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cheapestButton);
        }
    }

    @Step("Go to cart")
    public void goToCart() {
        logStep("Going to cart");
        driver.findElement(By.xpath("//button[contains(@onclick,'goToCart')]")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table tbody tr")));
    }

    @Step("Click Pay with Card")
    public void clickPayWithCard() {
        logStep("Clicking 'Pay with Card' button");
        WebElement payButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.stripe-button-el")));
        payButton.click();
    }

    @Step("Fill payment details")
    public void fillPaymentDetails() {
        logStep("Switching to Stripe iframe");
        WebElement stripeFrame = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("iframe.stripe_checkout_app"))
        );
        driver.switchTo().frame(stripeFrame);

        logStep("Filling email");
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));

        typeSlowly(emailInput, "test@test.com", 50);

        logStep("Filling card details");

        // Fill card number
        WebElement cardNumber = driver.findElement(By.id("card_number"));
        typeSlowly(cardNumber, "4242424242424242", 50);

        // Fill expiry
        WebElement cardExp = driver.findElement(By.id("cc-exp"));
        typeSlowly(cardExp, "12/34", 50);

        // Fill CVC
        WebElement cardCVC = driver.findElement(By.id("cc-csc"));
        typeSlowly(cardCVC, "123", 50);

        WebElement postalList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("billing-zip")));
        typeSlowly(postalList,"12345", 50);

        logStep("Submitting payment");
        WebElement payButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submitButton")));
        payButton.click();

        driver.switchTo().defaultContent();
    }

    public void logStep(String message) {
        System.out.println("[STEP] " + message);
        Allure.step(message);
    }

    public void typeSlowly(WebElement element, String text, long pauseMillis) {
        Actions actions = new Actions(driver);
        actions.click(element); // focus on the input
        for (char c : text.toCharArray()) {
            actions.sendKeys(String.valueOf(c))
                    .pause(Duration.ofMillis(pauseMillis));
        }
        actions.build().perform();
    }
}
