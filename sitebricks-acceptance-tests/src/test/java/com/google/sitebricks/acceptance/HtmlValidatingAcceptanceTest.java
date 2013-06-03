package com.google.sitebricks.acceptance;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.google.sitebricks.acceptance.page.HtmlValidatingPage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;

/**
 * 
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class HtmlValidatingAcceptanceTest {

  public void shouldGetValidationViolations() {
    WebDriver driver = AcceptanceTest.createWebDriver();
    HtmlValidatingPage page = HtmlValidatingPage.open(driver);

    List<String> expectedValidationViolations = Arrays.asList(
        "Constraint Violation Length First Name Message",
        "Constraint Violation Null Age Message",
        "Constraint Violation Length Last Name Message");

    List<String> actualValidationViolations = page.getValidationViolations();

    assert CollectionUtils.isEqualCollection(expectedValidationViolations, actualValidationViolations)
        : "validation violations didn't match what was expected";
    
  }

}
