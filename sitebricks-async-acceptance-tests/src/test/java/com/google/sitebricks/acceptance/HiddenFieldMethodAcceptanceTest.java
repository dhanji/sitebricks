package com.google.sitebricks.acceptance;

import com.google.sitebricks.acceptance.page.HiddenFieldMethodPage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

/**
 * @author Peter Knego
 */

@Test(suiteName = AcceptanceTest.SUITE)
public class HiddenFieldMethodAcceptanceTest {

  public void shouldRenderDynamicTextFromTextFieldBinding() {
    WebDriver driver = AcceptanceTest.createWebDriver();
    HiddenFieldMethodPage page = HiddenFieldMethodPage.open(driver);

    page.enterText("just some text");
    page.submitPut();

    // was the message generated via PUT method?
    assert page.isPutMessage();
  }
}
