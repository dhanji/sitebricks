package com.google.sitebricks.acceptance.page;

import com.google.sitebricks.acceptance.util.AcceptanceTest;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

/**
 * @author Peter Knego
 */
public class HiddenFieldMethodPage {

  private WebDriver driver;

  public HiddenFieldMethodPage(WebDriver driver) {
    this.driver = driver;
  }

  // "clicks"  the submit button
  public void submitPut() {
    driver.findElement(By.id("put"))
        .submit();
  }

  public void enterText(String text) {
    driver.findElement(By.name("text"))
        .sendKeys(text);
  }

  public boolean isPutMessage() {
    return driver.findElement(By.id("putMessage"))
        .getText()
        .endsWith("PUT");
  }

  public static HiddenFieldMethodPage open(WebDriver driver) {
    driver.get(AcceptanceTest.baseUrl() + "/hiddenfieldmethod");
    return PageFactory.initElements(driver, HiddenFieldMethodPage.class);
  }

}
