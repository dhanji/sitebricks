package com.google.sitebricks.acceptance;

import com.google.sitebricks.acceptance.page.HelloWorldPage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class HelloWorldAcceptanceTest {

  public void shouldRenderDynamicTextFromHelloWorld() {
    WebDriver driver = AcceptanceTest.createWebDriver();
    HelloWorldPage page = HelloWorldPage.open(driver, "/hello");

    assertHelloWorldContent(page);
  }

  public void shouldRenderDynamicTextFromHelloWorldService() {
    WebDriver driver = AcceptanceTest.createWebDriver();
    HelloWorldPage page = HelloWorldPage.open(driver, "/helloservice");

    assertHelloWorldContent(page);
  }

  public void shouldRenderDynamicTextFromHelloWorldServiceDirect() {
    WebDriver driver = AcceptanceTest.createWebDriver();
    HelloWorldPage page = HelloWorldPage.open(driver, "/helloservice/direct");

    assertHelloWorldContent(page);
  }

  private void assertHelloWorldContent(HelloWorldPage page) {
    assert page.hasHelloWorldMessage() : "Did not generate dynamic text from el expression";
    assert page.hasCorrectDoctype() : "Did not contain the expected doctype declaration at the start of the HTML file";
    assert page.hasMangledString() : "Did not contain method-generated string";
    assert page.hasNonSelfClosingScriptTag() : "Did not contain proper script tag with closing tag";
  }
}