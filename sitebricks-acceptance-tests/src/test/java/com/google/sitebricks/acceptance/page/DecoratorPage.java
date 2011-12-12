package com.google.sitebricks.acceptance.page;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

import com.google.sitebricks.acceptance.util.AcceptanceTest;

public class DecoratorPage {

  private WebDriver driver;

  public DecoratorPage(WebDriver driver) {
    this.driver = driver;
  }
  public boolean hasBasePageText() {
    return driver.getPageSource().contains("Text defined in");
  }
  public boolean hasBasePageVariable() {
    return driver.getPageSource().contains("from the superclass");
  }
  public boolean hasSubclassVariableInTemplate() {
	    return driver.getPageSource().contains("This comes from the subclass");
  }
  public boolean hasSubclassVariable() {
	    return driver.getPageSource().contains("very cool");
  }
  
  public boolean hasSubclassText() {
	    return driver.getPageSource().contains("This is in the extension");
	 }

  public static DecoratorPage open(WebDriver driver) {
    driver.get(AcceptanceTest.BASE_URL + "/template");
    return PageFactory.initElements(driver, DecoratorPage.class);
  }
}
