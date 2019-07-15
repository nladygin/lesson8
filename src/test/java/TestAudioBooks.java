import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.*;
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


public class TestAudioBooks {

    private static WebDriver driver;
    private static final String rootURL  = "https://www.mann-ivanov-ferber.ru/books/allbooks/?booktype=audiobook";
    WebDriverWait wait = new WebDriverWait(driver,3L);
    private Logger logger = LogManager.getLogger(TestAudioBooks.class);
    private static BufferedWriter writer;






    @Test
    public void go(){

        setTimeouts(10L);
        driver.get(rootURL);

        List<String> booksList = getAllBooks();
        logger.info("Books found: " + booksList.size());

        getBooksInfo(booksList);

    }




    private List<String> getAllBooks() {

        String bookLocator = "a[class='lego-book__cover js-image-block']";

        List<WebElement> list = driver.findElements(By.cssSelector(bookLocator));

        do {

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", driver.findElement(By.cssSelector("button[ng-click='loadNextPage()']")));

                try {
                    wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector(bookLocator), list.size()));
                } catch (TimeoutException e) {
                    break;
                }

                List<WebElement> tmp = driver.findElements(By.cssSelector(bookLocator));

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





    private void getBooksInfo(List<String> books) {

        for (String book : books) {

            driver.get(book);

            try {

                String title;
                try {
                    title = driver.findElement(By.cssSelector("h1[class='header active p-sky-title']")).getText();
                } catch (Exception e) {
                    title = "";
                }

                String author;
                try {
                    author = driver.findElement(By.cssSelector("div[class='authors']")).getText();
                } catch (Exception e) {
                    author = "";
                }

                String price;
                try {
                    price = driver.findElement(By.cssSelector("div[ng-if='bookData.types.audiobook.sale']")).getAttribute("data-price");
                } catch (Exception e) {
                    price = "";
                }

                String preview;
                try {
                    preview = driver.findElement(By.cssSelector("div[class='p-book-download-link m-audio-mp3 ng-scope'] a")).getAttribute("href");
                } catch (Exception e) {
                    preview = "";
                }

                logger.info("\"" + book + "\",\""
                        + title + "\",\""
                        + author + "\",\""
                        + price + "\",\""
                        + preview + "\"");

/*
                writer.write(book + ","
                        + title + ","
                        + author + ","
                        + price + ","
                        + preview);
                writer.newLine();
                writer.flush();
*/
            } catch (Exception e) {
                logger.error("parsing problem: " + book);
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
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("AudioBooksData.csv"), StandardCharsets.UTF_8));
    }



    @AfterClass
    public static void teardown() throws IOException {
        if (driver != null) {
            driver.quit();
        }
        writer.close();
    }

}