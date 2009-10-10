package com.google.sitebricks.acceptance;

import com.google.sitebricks.acceptance.page.CasePage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

/**
 * @author Tom Wilson (tom@tomwilson.name)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class CaseAcceptanceTest {
    public void shouldDisplayGreenFromCaseStatement() {
        WebDriver driver = AcceptanceTest.createWebDriver();
        CasePage page = CasePage.open(driver);

        assert "Green".equals(page.getDisplayedColor()) : "expected color wasn't displayed";
    }
}
