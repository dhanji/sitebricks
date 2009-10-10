package com.google.sitebricks.acceptance;

import com.google.sitebricks.acceptance.page.PageChainPage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class PageChainingAcceptanceTest {
  private static final String SOME_TEXT = "some random textina" + new Date();

  public void shouldPassEnteredTextToNextPage() {
    WebDriver driver = AcceptanceTest.createWebDriver();
    PageChainPage page = PageChainPage.open(driver);

    page.enterText(SOME_TEXT);
    page.next();

    // We should now be on the NextPage page
    assert driver.findElement(By.xpath("//div[@class='entry']"))
        .getText()
        .contains(SOME_TEXT)
        : "Value did not get passed via page chaining to next page";
  }
}
