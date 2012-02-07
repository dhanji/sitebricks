package com.google.sitebricks.acceptance;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.google.sitebricks.acceptance.page.VelocitySamplePage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;

@Test(suiteName = AcceptanceTest.SUITE)
public class VelocitySampleAcceptanceTest {

    public void shouldRenderDynamicTextFromVelocitySample() {
        WebDriver driver = AcceptanceTest.createWebDriver();
        VelocitySamplePage page = VelocitySamplePage.open(driver, "/velocitySample");

        assertVelocitySampleContent(page);
    }

    private void assertVelocitySampleContent(VelocitySamplePage page) {
        String title = page.getTitle();
        assert title.equals("velocity sample") : title + " != \"velocity sample\"\n" + page.getContent();
        assert page.hasMessage() : "did not have dynamic text";
    }
}
