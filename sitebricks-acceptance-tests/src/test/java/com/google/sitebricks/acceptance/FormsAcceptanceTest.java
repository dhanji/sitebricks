package com.google.sitebricks.acceptance;

import com.google.sitebricks.acceptance.page.FormsPage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class FormsAcceptanceTest {
  private static final String SOME_TEXT = "aoskdopaksdoaskd" + new Date();

  public void shouldRenderDynamicTextFromTextFieldBinding() {
    WebDriver driver = AcceptanceTest.createWebDriver();
    FormsPage page = FormsPage.open(driver);

    final String boundAutobots = "Optimus, Rodimus, UltraMagnus";
    final String[] strings = boundAutobots.split(", ");

    page.enterText(SOME_TEXT);
    page.enterAutobots(strings[0], strings[1], strings[2]);
    page.send();

    assert page.hasBoundText(SOME_TEXT) : "Did not generate dynamic text from form binding";
    assert page.hasBoundAutobots(boundAutobots) : "Did not generate text from list binding";
  }
}
