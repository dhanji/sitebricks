package com.google.sitebricks.acceptance.page;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import com.google.sitebricks.acceptance.util.AcceptanceTest;

public class CustomConversionPage {

  private WebElement testValue;

  public static CustomConversionPage open(WebDriver driver) {
    driver.get(AcceptanceTest.BASE_URL + "/customConvertion");
    return PageFactory.initElements(driver, CustomConversionPage.class);
  }

  public WebElement getTestValue() {
    return testValue;
  }

}
