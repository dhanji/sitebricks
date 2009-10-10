package com.google.sitebricks.acceptance;

import com.google.sitebricks.acceptance.page.DynamicJsPage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class DynamicJsAcceptanceTest {

    public void shouldRenderDynamicTextFromJsTemplate() {
        WebDriver driver = AcceptanceTest.createWebDriver();
        DynamicJsPage page = DynamicJsPage.open(driver);

        assert page.hasDynamicText() : "Did not generate dynamic text from warp-widget";
    }
}