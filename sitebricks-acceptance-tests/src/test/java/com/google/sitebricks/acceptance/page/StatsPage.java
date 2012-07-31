package com.google.sitebricks.acceptance.page;

import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.example.Start;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class StatsPage {
  private WebDriver driver;

  public StatsPage(WebDriver driver) {
    this.driver = driver;
  }

  public boolean hasNonZeroStats() {
    String pageSource = driver.getPageSource();
    int pageLoadsStart = pageSource.indexOf(Start.PAGE_LOADS);
    // the format is as follows: <b>label:</b>value<br/>
    pageLoadsStart += Start.PAGE_LOADS.length();
    pageLoadsStart += ":</b>".length();

    int value = Integer.parseInt(
        pageSource.substring(pageLoadsStart, pageSource.indexOf("<br/>")).trim());

    return value > 0;
  }

  public static StatsPage open(WebDriver driver) {
    driver.get(AcceptanceTest.baseUrl() + "/stats");
    return PageFactory.initElements(driver, StatsPage.class);
  }
}