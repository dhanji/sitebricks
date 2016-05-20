package com.google.sitebricks.acceptance;

import com.google.sitebricks.acceptance.page.EmbeddedRepeatPage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import java.util.List;

/**
 * author: Martins Barinskis (martins.barinskis@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class EmbeddedRepeatTest extends SitebricksJettyAcceptanceTest {

    public void shouldRenderPassedParameterInEveryItem() {
        WebDriver driver = AcceptanceTest.createWebDriver();
        EmbeddedRepeatPage page = EmbeddedRepeatPage.open(driver);
        List<String> items = page.getItems();
        assert items.size() == 3;
        assert "first item".equals(items.get(0));
        assert "second item".equals(items.get(1));
        assert "third item".equals(items.get(2));
    }

}
