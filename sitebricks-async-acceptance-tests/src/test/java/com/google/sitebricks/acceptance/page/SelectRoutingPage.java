package com.google.sitebricks.acceptance.page;

import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.example.HelloWorld;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

public class SelectRoutingPage {

  private WebDriver driver;

  public SelectRoutingPage(WebDriver driver) {
    this.driver = driver;
  }

  public boolean hasExpectedDiv(String className) {
    try {
      WebElement element = driver.findElement(By.className(className));
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  public boolean hasExpectedDivCount(int i) {
    List<WebElement> elements = driver.findElements(By.className("result"));
    return elements.size() == i;
  }

  public static SelectRoutingPage open(WebDriver driver) {
    driver.get(AcceptanceTest.BASE_URL + "/select");
    return PageFactory.initElements(driver, SelectRoutingPage.class);
  }

  public void submit(String s) {
    driver.findElement(By.id(s + "Submit")).submit();    
  }
}
