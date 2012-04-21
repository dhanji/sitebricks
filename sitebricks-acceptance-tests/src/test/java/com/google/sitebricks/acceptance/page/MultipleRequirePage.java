package com.google.sitebricks.acceptance.page;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;

import com.google.sitebricks.acceptance.util.AcceptanceTest;

/**
 * @author Ross Judson (rossjudson@gmail.com)
 */
public class MultipleRequirePage {

  @FindBy(how = How.XPATH, using = "//head/script[@class='twice'][1]")
  private WebElement first;
  
  @FindBy(how = How.XPATH, using = "//head/script[@class='twice'][2]")
  private WebElement second;

  private WebDriver driver;

  public MultipleRequirePage(WebDriver driver) {
    this.driver = driver;
  }

  public boolean isFirstPresent() {
      return first != null;
  }
  
  public boolean isSecondPresent() {
      return second != null;
  }
  
  public static MultipleRequirePage open(WebDriver driver) {
    driver.get(AcceptanceTest.BASE_URL + "/multipleembed");
    return PageFactory.initElements(driver, MultipleRequirePage.class);
  }
}
