package com.google.sitebricks.acceptance.page;

import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class EmbedPage {
  private static final String EMBED_MSG = "Message : " +
      "Embedding in google-sitebricks is awesome!";

  private WebDriver driver;

  public EmbedPage(WebDriver driver) {
    this.driver = driver;
  }

  public boolean hasCssLink() {
    return null != driver.findElement(By.xpath("//head/link[@href='default.css']"));
  }


  public boolean hasHelloWorldMessage() {
    return driver.getPageSource().contains(EMBED_MSG);
  }

  public static EmbedPage open(WebDriver driver) {
    driver.get(AcceptanceTest.BASE_URL + "/embed");
    return PageFactory.initElements(driver, EmbedPage.class);
  }
}
