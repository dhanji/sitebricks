package com.google.sitebricks.acceptance;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.google.sitebricks.acceptance.page.CustomConversionPage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.example.CustomConverter;

@Test(suiteName = AcceptanceTest.SUITE)
public class CustomConverterAcceptanceTest {

  public void hasConvertedTypes() {
    WebDriver driver = AcceptanceTest.createWebDriver();
    CustomConversionPage page = CustomConversionPage.open(driver);
    
    WebElement testValueInputField = page.getTestValue();
    String testValue = testValueInputField.getAttribute("value");
    assert testValue.equals(CustomConverter.INITIAL_VALUE) : "expected " + CustomConverter.INITIAL_VALUE + " but was " + testValue;
    
    
    String expected = "new value from test";
    testValueInputField.sendKeys(expected);
    testValueInputField.submit();
    System.out.println(driver.getPageSource());

    assert testValueInputField.getAttribute("value").equals(CustomConverter.INITIAL_VALUE + expected) : "expected " + CustomConverter.INITIAL_VALUE + expected + " but was " + testValue;
  }
}
