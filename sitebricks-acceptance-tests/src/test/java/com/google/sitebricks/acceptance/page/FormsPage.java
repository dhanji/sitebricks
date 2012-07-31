package com.google.sitebricks.acceptance.page;

import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

public class FormsPage {

  private WebDriver driver;

  public FormsPage(WebDriver driver) {
    this.driver = driver;
  }

  public void enterText(String text) {
    driver.findElement(By.name("text"))
        .sendKeys(text);
  }

  public void send() {
    driver.findElement(By.id("send"))
        .submit();
  }

  public boolean hasBoundText(String someText) {
    return driver.findElement(By.id("boundText"))
        .getText()
        .contains(someText);
  }

  public static FormsPage open(WebDriver driver) {
    driver.get(AcceptanceTest.baseUrl() + "/forms");
    return PageFactory.initElements(driver, FormsPage.class);
  }

  public void enterAutobots(String s1, String s2, String s3) {
    final List<WebElement> autobotTextFields = driver.findElements(By.name("autobots"));

    autobotTextFields.get(0).sendKeys(s1);
    autobotTextFields.get(1).sendKeys(s2);
    autobotTextFields.get(2).sendKeys(s3);
  }

  public boolean hasBoundAutobots(String expected) {
    return driver.findElement(By.id("boundAutobots"))
        .getText()
        .contains(expected);

  }
}
