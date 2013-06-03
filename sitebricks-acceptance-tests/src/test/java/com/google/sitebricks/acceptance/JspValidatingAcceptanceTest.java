package com.google.sitebricks.acceptance;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.google.sitebricks.acceptance.page.JspValidatingPage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;

/**
 * 
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class JspValidatingAcceptanceTest {

  public void shouldGetValidationViolations() {
    WebDriver driver = AcceptanceTest.createWebDriver();
    JspValidatingPage page = JspValidatingPage.open(driver);

    List<String> expectedValidationViolations = Arrays.asList(
        "constraintViolationLengthFirstName",
        "constraintViolationNullAge",
        "constraintViolationLengthLastName");

    List<String> actualValidationViolations = page.getValidationViolations();

    assert CollectionUtils.isEqualCollection(expectedValidationViolations, actualValidationViolations)
        : "validation violations didn't match what was expected";
  }

}
