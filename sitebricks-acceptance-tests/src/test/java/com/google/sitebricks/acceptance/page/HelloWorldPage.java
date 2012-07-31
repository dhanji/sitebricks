package com.google.sitebricks.acceptance.page;

import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.example.HelloWorld;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class HelloWorldPage {

  private WebDriver driver;

  public HelloWorldPage(WebDriver driver) {
    this.driver = driver;
  }

  public boolean hasHelloWorldMessage() {
    //TODO ugh! stupid xpath doesn't work =(
    return driver.getPageSource().contains(HelloWorld.HELLO_MSG);
  }

  public boolean hasCorrectDoctype() {
    return driver.getPageSource().startsWith("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n" +
        "    \"http://www.w3.org/TR/html4/loose.dtd\">");
  }

  public boolean hasMangledString() {
    return driver.getPageSource().contains(new HelloWorld().mangle(HelloWorld.HELLO_MSG));
  }

  public static HelloWorldPage open(WebDriver driver, String url) {
    driver.get(AcceptanceTest.baseUrl() + url);
    return PageFactory.initElements(driver, HelloWorldPage.class);
  }

  public boolean hasNonSelfClosingScriptTag() {
    return driver.getPageSource().contains("<script type=\"text/javascript\"></script>");
  }
}