package com.google.sitebricks.acceptance;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.google.sitebricks.acceptance.page.MultipleRequirePage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;

/**
 * @author Ross Judson (rossjudson@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class MultipleRequireAcceptanceTest {
    public void shouldContainOnlyOneCopyOfRequire() {
        WebDriver driver = AcceptanceTest.createWebDriver();
        MultipleRequirePage page = MultipleRequirePage.open(driver);
        
        assert page.isFirstPresent() : "Should have found at least one @Require entry";
        assert !page.isSecondPresent() : "Should not have found a second copy of the Require entry";
        
    }
}
