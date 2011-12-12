package com.google.sitebricks.acceptance;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.google.sitebricks.acceptance.page.DecoratorPage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class DecoratorAcceptanceTest {

  public void shouldRenderHelloWorldEmbeddedWithRequires() {
    WebDriver driver = AcceptanceTest.createWebDriver();
    DecoratorPage page = DecoratorPage.open(driver);

    assert page.hasBasePageText();
    assert page.hasSubclassVariable();
    assert page.hasSubclassText(); 
    assert page.hasSubclassVariableInTemplate();
    assert page.hasBasePageVariable();
  }
}