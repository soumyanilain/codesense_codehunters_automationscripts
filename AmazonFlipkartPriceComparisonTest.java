package com.gw_msi.Regression;

        import com.aventstack.extentreports.ExtentTest;
        import com.aventstack.extentreports.Status;
        import com.gw_msi.base_utils.CommonResuableHelperClass;
        import com.gw_msi.utils.BasePage;
        import io.restassured.RestAssured;
        import io.restassured.http.Method;
        import io.restassured.path.json.JsonPath;
        import io.restassured.response.Response;
        import io.restassured.specification.RequestSpecification;
        import org.openqa.selenium.By;
        import org.openqa.selenium.Keys;
        import org.openqa.selenium.WebDriver;
        import org.openqa.selenium.chrome.ChromeDriver;
        import org.openqa.selenium.edge.EdgeDriver;
        import org.openqa.selenium.interactions.Actions;
        import org.testng.annotations.*;

        import java.io.IOException;
        import java.util.HashMap;

        import static com.gw_msi.utils.ReportsExtent.extent;
        import static com.gw_msi.utils.ReportsExtent.spark;

public class AmazonFlipkartPriceComparisonTest extends CommonResuableHelperClass {

    private final String testScenarioName = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1);
    public static ExtentTest test = extent.createTest("CodeHuntersTest");
    WebDriver driver;
    String fifthProduct;
    double amazonPriceInINR;
    double flipkart_price;

    String flipkartTitle;
    double flipkartPriceInINR;

    @BeforeTest
    public void setUp() {
        extent.attachReporter(spark);
    }

    @Ignore
    @Test(priority = 1)
    public void getProductsFromApiTest() {
        ExtentTest node = test.createNode(getCurrentInvokedMethodName());
        HashMap<String, String> queryParameters = new HashMap<>();
        queryParameters.put("index", "4");

        RestAssured.baseURI = "";
        RequestSpecification httpRequest = RestAssured.given();
        httpRequest.header("Content-Type", "application/json");
        Response response = httpRequest.queryParams(queryParameters).request(Method.GET);

        if (response.statusCode() == 200)
            node.log(Status.PASS, "The actual API response status code is " + "<strong style=\"color:blue;\">" + response.statusCode() + "</strong>" + " and matched with the expected status code " + "<strong style=\"color:blue;\">" + "200" + "</strong>");
        else
            node.log(Status.FAIL, "The actual API response status code is " + "<strong style=\"color:red;\">" + response.statusCode() + "</strong>" + " but didn't match with the expected status code " + "<strong style=\"color:blue;\">" + "200" + "</strong>");

        JsonPath jp = new JsonPath(response.asString());
        String totalProducts = getJsonUtils().getTotalNodes(response);
        System.out.println(totalProducts);

        fifthProduct = jp.getString("[4].name");

        node.log(Status.INFO, "Fifth product name = " + "<strong style=\"color:blue;\">" + fifthProduct + "</strong>");

    }

    @Test(priority = 1)
    public void flipkart() throws InterruptedException {

        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://www.flipkart.com");

        //Actions act = new Actions(driver);
        //act.sendKeys(Keys.ESCAPE).build().perform();
        Thread.sleep(500);

        //By closeicon = new By.ByXPath("//button[contains(text(),'✕')]");
        //driver.findElement(closeicon).click();

        flipkartTitle = driver.getTitle();
        Thread.sleep(3000);
        //By searchbar = new By.ByXPath("(//a[contains(text(),'Become a Seller')]//parent::div//preceding::input)[1]");
        By searchbar = new By.ByXPath("//input[@name='q']");
        driver.findElement(searchbar).sendKeys("iphone 14 128GB BLUE", Keys.ENTER);

        Thread.sleep(3000);
        By applephone = new By.ByXPath("((//div[contains(text(),'APPLE iPhone 14 (Blue, 128 GB)')]//parent::div//following::div)[4]//div[contains(text(),'₹')])[1]");
        String priceOfApple14 = driver.findElement(applephone).getText().trim().replaceAll("[^0-9]","").replace(",","");

        flipkart_price = Double.parseDouble(priceOfApple14);
        System.out.println("price of iphone 14 128GB on flipkart is : "+flipkart_price);
    }

    @Test(priority = 2)
    public void getPriceFromAmazonTest() throws InterruptedException {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://www.amazon.in/");

        driver.findElement(By.xpath("//input[contains(@placeholder,'Search Amazon')]")).sendKeys("iPhone 14 128 gb blue");
        driver.findElement(By.xpath("//input[@value='Go']")).click();

        Thread.sleep(5000);
        String strPrice = driver.findElement(By.xpath("(//span[@class='a-price-whole'])[1]")).getText().trim().replaceAll("[^0-9]","").replace(",","");
        amazonPriceInINR = Double.parseDouble(strPrice);
        System.out.println(amazonPriceInINR);
    }

    @Test(priority = 3)
    public void ComparePricesAndCheckout() throws InterruptedException {
        if(amazonPriceInINR<flipkart_price){
            driver.findElement(By.xpath("//span[contains(text(),'Apple iPhone 14 (128 GB) - Blue')]//parent::a")).click();
            Thread.sleep(3000);
            driver.switchTo().window("Apple iPhone 14 (128 GB) - Blue : Amazon.in: Electronics");
            driver.findElement(By.xpath("//input[@value='Add to Cart']")).click();
        }else{
            driver.switchTo().window(flipkartTitle);
            driver.findElement(By.xpath("//div[contains(text(),'APPLE iPhone 14 (Blue, 128 GB)')]//parent::div//ancestor::a")).click();
        }
    }
    @AfterTest
    public void tearDown() throws IOException {
        driver.quit();
        extent.flush();
    }

    public String getCurrentInvokedMethodName() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        if (stackTrace.length >= 2) {
            return stackTrace[1].getMethodName();
        } else {
            return null; // No method found
        }
    }

}
