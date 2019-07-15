import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class TestDrive2 {

    private static WebDriver driver;
    private static final String rootURL  = "https://www.drive2.ru/cars/?sort=selling";
    WebDriverWait wait = new WebDriverWait(driver,10L);
    private Logger logger = LogManager.getLogger(TestDrive2.class);
    private static BufferedWriter writer;






    @Test
    public void go(){

        List<String> carModelList = new ArrayList<>();

        setTimeouts(60L);
        driver.get(rootURL);

        /* show full car model list */
        driver.findElement(By.cssSelector("button.c-block__more")).click();
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("a[class='c-link c-link--text']"), 50));

        /* get all car model links */
        carModelList = driver.findElements(By.cssSelector("a[class='c-link c-link--text']"))
                            .stream()
                            .map(element -> element.getAttribute("href"))
                            .collect(Collectors.toList());

        for (String carModel: carModelList) {
            List<String> cars = getCarList(carModel);
            collectData(cars);
        }

    }



    private List<String> getCarList(String carModel){

        String carLocator = "a[class='u-link-area']";

        driver.get(carModel);

            setTimeouts(5L);
            List<WebElement> list = driver.findElements(By.cssSelector(carLocator));
            if (list.size() == 0) { return null; }
            setTimeouts(60L);

                do {
                    Actions actions = new Actions(driver);
                    actions.moveToElement(list.get(list.size()-1)).perform();

                    List<WebElement> tmp = driver.findElements(By.cssSelector(carLocator));

                    if (tmp.size() > list.size()) {
                        list = tmp;
                    } else {
                        break;
                    }

                } while (true);

            return list.stream()
                    .map(element->element.getAttribute("href"))
                    .collect(Collectors.toList());
    }




    private void collectData(List<String> cars) {

        if (cars == null) { return; }

            for (int i=0; i<cars.size(); i++) {

                driver.get(cars.get(i));

                List<WebElement> info = driver.findElements(By.cssSelector("ul[class='c-car-forsale__info'] li"));

                try {
                    String engine = info.get(1).getText().replaceAll(",","");
                    String year = info.get(4).getText().replaceAll(",","");

                    String price = driver.findElement(By.cssSelector("div[class='c-car-forsale__price'] strong")).getText().replaceAll("[,|\u20BD]","");
                    String mark = driver.findElement(By.cssSelector("a[data-ym-target='car2brand'")).getText().replaceAll(",","");
                    String model = driver.findElement(By.cssSelector("a[data-ym-target='car2model'")).getText().replaceAll(",","");

                    writer.write(cars.get(i) + ","
                                    + year + ","
                                    + price + ","
                                    + mark + ","
                                    + model + ","
                                    + engine);
                    writer.newLine();
                    writer.flush();

                } catch (Exception e) {
                    logger.error("parsing problem: " + cars.get(i));
                }
            }
    }






    private void setTimeouts(long timeout) {
        driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(timeout, TimeUnit.SECONDS);
    }



    @BeforeClass
    public static void setup() throws IOException {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("DriveData.csv"), StandardCharsets.UTF_8));
    }



    @AfterClass
    public static void teardown() throws IOException {
        if (driver != null) {
            driver.quit();
        }
        writer.close();
    }

}
