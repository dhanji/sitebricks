package com.google.sitebricks.acceptance.page;

import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class PageChainPage {

  private WebDriver driver;

  public PageChainPage(WebDriver driver) {
    this.driver = driver;
  }


  public static PageChainPage open(WebDriver driver) {
    driver.get(AcceptanceTest.baseUrl() + "/pagechain");
    return PageFactory.initElements(driver, PageChainPage.class);
  }

  public void enterText(String someText) {
    driver.findElement(By.name("userValue"))
        .sendKeys(someText);
  }

  public void next() {
    driver.findElement(By.id("send"))
        .submit();
  }
}
