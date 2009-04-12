package com.google.sitebricks.acceptance;

import com.google.sitebricks.acceptance.page.FormsPage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class FormsAcceptanceTest {
    private static final String SOME_TEXT = "aoskdopaksdoaskd" + new Date();

    public void shouldRenderDynamicTextFromTextFieldBinding() {
        WebDriver driver = AcceptanceTest.createWebDriver();
        FormsPage page = FormsPage.open(driver);

        page.enterText(SOME_TEXT);

        assert page.hasBoundText(SOME_TEXT) : "Did not generate dynamic text from form binding";
    }
}