package com.google.sitebricks.acceptance;

import com.google.sitebricks.acceptance.page.SelectRoutingPage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

@Test(suiteName = AcceptanceTest.SUITE)
public class SelectRoutingAcceptanceTest {
  
  public void shouldRenderDivForDefaultGetOnly() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForDefaultGetOnly");
    assert page.hasExpectedDiv("defaultGet");
    assert page.hasExpectedDivCount(1);
  }

  public void shouldRenderDivForFooGetOnly() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForFooGetOnly");
    assert page.hasExpectedDiv("fooGet");
    assert page.hasExpectedDivCount(1);
  }

  public void shouldRenderDivForBarGetOnly() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForBarGetOnly");
    assert page.hasExpectedDiv("barGet");
    assert page.hasExpectedDivCount(1);
  }

  public void shouldRenderDivForFooBarGet() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForFooBarGet");

    assert page.hasExpectedDiv("fooGet");
    assert page.hasExpectedDiv("barGet");
    assert page.hasExpectedDivCount(2);
  }

  public void shouldRenderDivForUnknownGet() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForUnknownGet");
    assert page.hasExpectedDiv("defaultGet");
    assert page.hasExpectedDivCount(1);
  }

  public void shouldRenderDivForUnknownAndFooGet() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForUnknownAndFooGet");
    assert page.hasExpectedDiv("fooGet");
    assert page.hasExpectedDivCount(1);
  }

  public void shouldRenderDivForRedirectGet() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForRedirectGet");
    assert page.hasExpectedDiv("defaultGet");
    assert page.hasExpectedDiv("redirectGet");
    assert page.hasExpectedDivCount(2);
  }

  public void shouldRenderDivForDefaultPostOnly() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForDefaultPostOnly");
    assert page.hasExpectedDiv("defaultPost");
    assert page.hasExpectedDivCount(1);
  }

  public void shouldRenderDivForFooPostOnly() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForFooPostOnly");
    assert page.hasExpectedDiv("fooPost");
    assert page.hasExpectedDivCount(1);
  }

  public void shouldRenderDivForBarPostOnly() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForBarPostOnly");
    assert page.hasExpectedDiv("barPost");
    assert page.hasExpectedDivCount(1);
  }

  public void shouldRenderDivForFooBarPost() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForFooBarPost");

    assert page.hasExpectedDiv("fooPost");
    assert page.hasExpectedDiv("barPost");
    assert page.hasExpectedDivCount(2);
  }

  public void shouldRenderDivForUnknownPost() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForUnknownPost");
    assert page.hasExpectedDiv("defaultPost");
    assert page.hasExpectedDivCount(1);
  }

  public void shouldRenderDivForUnknownAndFooPost() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForUnknownAndFooPost");
    assert page.hasExpectedDiv("fooPost");
    assert page.hasExpectedDivCount(1);
  }

  public void shouldRenderDivForRedirectPost() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForRedirectPost");
    assert page.hasExpectedDiv("defaultGet");
    assert page.hasExpectedDiv("redirectPost");
    assert page.hasExpectedDivCount(2);
  }


  public void shouldRenderDivForDefaultPutOnly() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForDefaultPutOnly");
    assert page.hasExpectedDiv("defaultPut");
    assert page.hasExpectedDivCount(1);
  }

  public void shouldRenderDivForFooPutOnly() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForFooPutOnly");
    assert page.hasExpectedDiv("fooPut");
    assert page.hasExpectedDivCount(1);
  }

  public void shouldRenderDivForBarPutOnly() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForBarPutOnly");
    assert page.hasExpectedDiv("barPut");
    assert page.hasExpectedDivCount(1);
  }

  public void shouldRenderDivForFooBarPut() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForFooBarPut");

    assert page.hasExpectedDiv("fooPut");
    assert page.hasExpectedDiv("barPut");
    assert page.hasExpectedDivCount(2);
  }

  public void shouldRenderDivForUnknownPut() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForUnknownPut");
    assert page.hasExpectedDiv("defaultPut");
    assert page.hasExpectedDivCount(1);
  }

  public void shouldRenderDivForUnknownAndFooPut() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForUnknownAndFooPut");
    assert page.hasExpectedDiv("fooPut");
    assert page.hasExpectedDivCount(1);
  }
  
  public void shouldRenderDivForRedirectPut() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForRedirectPut");
    assert page.hasExpectedDiv("defaultGet");
    assert page.hasExpectedDiv("redirectPut");
    assert page.hasExpectedDivCount(2);
  }

  
  public void shouldRenderDivForDefaultDeleteOnly() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForDefaultDeleteOnly");
    assert page.hasExpectedDiv("defaultDelete");
    assert page.hasExpectedDivCount(1);
  }

  public void shouldRenderDivForFooDeleteOnly() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForFooDeleteOnly");
    assert page.hasExpectedDiv("fooDelete");
    assert page.hasExpectedDivCount(1);
  }

  public void shouldRenderDivForBarDeleteOnly() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForBarDeleteOnly");
    assert page.hasExpectedDiv("barDelete");
    assert page.hasExpectedDivCount(1);
  }

  public void shouldRenderDivForFooBarDelete() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForFooBarDelete");

    assert page.hasExpectedDiv("fooDelete");
    assert page.hasExpectedDiv("barDelete");
    assert page.hasExpectedDivCount(2);
  }

  public void shouldRenderDivForUnknownDelete() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForUnknownDelete");
    assert page.hasExpectedDiv("defaultDelete");
    assert page.hasExpectedDivCount(1);
  }


  public void shouldRenderDivForUnknownAndFooDelete() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForUnknownAndFooDelete");
    assert page.hasExpectedDiv("fooDelete");
    assert page.hasExpectedDivCount(1);
  }

  public void shouldRenderDivForRedirectDelete() {
    SelectRoutingPage page = loadPage();

    page.submit("shouldRenderDivForRedirectDelete");
    assert page.hasExpectedDiv("defaultGet");
    assert page.hasExpectedDiv("redirectDelete");
    assert page.hasExpectedDivCount(2);
  }

  private SelectRoutingPage loadPage() {
    WebDriver driver = AcceptanceTest.createWebDriver();
    return SelectRoutingPage.open(driver);
  }
}
