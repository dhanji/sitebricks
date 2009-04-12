package com.google.sitebricks.acceptance;

import com.google.sitebricks.acceptance.page.HelloWorldPage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class HelloWorldAcceptanceTest {

    public void shouldRenderDynamicTextFromHelloWorld() {
        WebDriver driver = AcceptanceTest.createWebDriver();
        HelloWorldPage page = HelloWorldPage.open(driver);

        assert page.hasHelloWorldMessage() : "Did not generate dynamic text from el expression";
    }
}