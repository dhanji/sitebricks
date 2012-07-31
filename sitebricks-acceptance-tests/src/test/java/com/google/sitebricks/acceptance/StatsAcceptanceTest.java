package com.google.sitebricks.acceptance;

import com.google.sitebricks.acceptance.page.StatsPage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class StatsAcceptanceTest {

  public void shouldRenderStatsForPageRequest() {
    WebDriver driver = AcceptanceTest.createWebDriver();

    // Request start page.
    driver.get(AcceptanceTest.baseUrl());

    StatsPage statsPage = StatsPage.open(driver);

    assert statsPage.hasNonZeroStats() : "Recorded stats should be at least 1";
  }
}