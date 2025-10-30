import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

public class DriverFactory {

    public static WebDriver createDriver(String browser, boolean remote) throws MalformedURLException {
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
        System.out.println("Browser: " + browser + " | Remote: " + remote + " | Headless: " + headless);

        if (remote) {
            String gridUrl = "http://localhost:4444/wd/hub";
            System.out.println("Connecting to Selenium Grid at " + gridUrl);
            return new RemoteWebDriver(new URL(gridUrl), getOptions(browser, headless));
        } else {
            System.out.println("Starting local WebDriver for " + browser);
            return createLocalDriver(browser, headless);
        }
    }

    private static WebDriver createLocalDriver(String browser, boolean headless) {
        switch (browser.toLowerCase()) {
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) firefoxOptions.addArguments("--headless");
                return new FirefoxDriver(firefoxOptions);

            case "edge":
                WebDriverManager.edgedriver().setup();
                EdgeOptions edgeOptions = new EdgeOptions();
                if (headless) edgeOptions.addArguments("--headless=new");
                return new EdgeDriver(edgeOptions);

            default: // Chrome
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                if (headless) chromeOptions.addArguments("--headless=new");
                return new ChromeDriver(chromeOptions);
        }
    }

    private static Capabilities getOptions(String browser, boolean headless) {
        return switch (browser.toLowerCase()) {
            case "firefox" -> {
                FirefoxOptions opts = new FirefoxOptions();
                if (headless) opts.addArguments("--headless");
                yield opts;
            }
            case "edge" -> {
                EdgeOptions opts = new EdgeOptions();
                if (headless) opts.addArguments("--headless=new");
                yield opts;
            }
            default -> {
                ChromeOptions opts = new ChromeOptions();
                if (headless) opts.addArguments("--headless=new");
                yield opts;
            }
        };
    }
}
