package com.google.sitebricks.acceptance.page;

import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.openqa.selenium.support.How;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class CasePage {

  @FindBy(how = How.XPATH, using = "//div[@class='entry']/font[@color='green']")
  private WebElement entry;

  private WebDriver driver;

  public CasePage(WebDriver driver) {
    this.driver = driver;
  }

  public String getDisplayedColor() {
    return entry.getText();
  }

  public static CasePage open(WebDriver driver) {
    driver.get(AcceptanceTest.BASE_URL + "/case");
    return PageFactory.initElements(driver, CasePage.class);
  }
}
