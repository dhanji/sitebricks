package com.google.sitebricks.acceptance;

import com.google.sitebricks.acceptance.page.DecoratedRepeatPage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.acceptance.util.JettyAcceptanceTest;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

/**
 * author: Martins Barinskis (martins.barinskis@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class DecoratedRepeatTest {
    public void shouldRenderPageFieldInsideRepeat() {
        WebDriver driver = AcceptanceTest.createWebDriver();
        driver.get(AcceptanceTest.baseUrl() + "/decorated-repeat");
        DecoratedRepeatPage page = DecoratedRepeatPage.open(driver);
        List<String> items = page.getItems();
        assert items.size() == 3;
        assert "Hello, one".equals(items.get(0));
        assert "Hello, two".equals(items.get(1));
        assert "Hello, three".equals(items.get(2));
    }
}
