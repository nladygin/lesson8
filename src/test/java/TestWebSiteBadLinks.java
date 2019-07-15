import io.github.bonigarcia.wdm.WebDriverManager;
import net.lightbody.bmp.BrowserMobProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.TimeoutException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class TestWebSiteBadLinks {

    private static WebDriver driver;
    private static BrowserMobProxy server;
    private Logger logger = LogManager.getLogger(TestWebSiteBadLinks.class);
    private String rootURL = "http://oldnewrock.ru/news"; //"https://www.hrsinternational.com/";




    @Test
    public void test(){
        SiteTree siteTree = new SiteTree(rootURL);
        logger.info("start dig");
        dig(siteTree);
        logger.info("start check [source pages: " + siteTree.size() + " | target pages: " + siteTree.getBranchCount() + "]");
        check(siteTree);
    }





    private void dig(SiteTree siteTree) {

        //getFirstCheckLink
        Map.Entry<String, List<String>> entry = siteTree.getNextSourcePage();

        if (entry == null) { return; }

        if (entry.getValue() == null) {
            String sourcePage = entry.getKey();
            try {
                driver.get(sourcePage);
                //get all href from a page
                List<String> hrefs =
                        driver.findElements(By.cssSelector("a[href]"))
                                .stream()
                                .map(element -> element.getAttribute("href"))
                                .distinct()
                                .filter(element -> !element.contains("@") && !element.contains("mailto"))
                                .collect(Collectors.toList());

                // add hrefs to list
                siteTree.addLinks(sourcePage, hrefs);
            } catch (TimeoutException e) {
                logger.error("cannot get page: " + sourcePage);
                siteTree.addLinks(sourcePage, new LinkedList<>());
            }
            siteTree.showTreeSize(logger);
            dig(siteTree);
        }

    }





    private void check(SiteTree siteTree) {
        Iterator hmIterator = siteTree.entrySet().iterator();
        do {
            SiteTree.Entry mapElement = (SiteTree.Entry) hmIterator.next();
            String sourcePage = (String) mapElement.getKey();
            List<String> targetPages = (List<String>) mapElement.getValue();
            for (String targetPage : targetPages) {
                if (targetPage.equals("")) { continue; }
                try {
                    URL url = new URL(targetPage);
                    HttpURLConnection http = (HttpURLConnection) url.openConnection();
                    int statusCode = http.getResponseCode();
                    if (statusCode >=400) {
                        logger.info(sourcePage + " -> " + targetPage + " [" + statusCode + "]");
                    }
                } catch (Exception e) {
                    logger.error(sourcePage + " -> " + targetPage + " [page check problem]");
                }
            }
        } while (hmIterator.hasNext());
    }












    @BeforeClass
    public static void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(10L, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(10L, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(10L, TimeUnit.SECONDS);
    }



    @AfterClass
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
