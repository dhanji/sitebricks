package com.google.sitebricks.acceptance;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.google.sitebricks.acceptance.page.JspPage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;

/**
 * 
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class JspAcceptanceTest {

  public void shouldRepeatItemsFromCollection() {
    WebDriver driver = AcceptanceTest.createWebDriver();
    JspPage page = JspPage.open(driver);

    List<String> expectedNames = Arrays.asList(
        "0: Dhanji (last? false)",
        "1: Josh (last? false)",
        "2: Jody (last? false)",
        "3: Iron Man (last? true)");
    List<String> expectedMovies =
        Arrays.asList("Dhanji Josh Jody Iron Man",
            "Dhanji Josh Jody Iron Man",
            "Dhanji Josh Jody Iron Man");

    List<String> actualNames = page.getRepeatedNames();
    List<String> actualMovies = page.getRepeatedMovies();

    assert CollectionUtils.isEqualCollection(expectedNames, actualNames)
        : "repeated names didn't match what was expected";
    assert CollectionUtils.isEqualCollection(expectedMovies, actualMovies)
        : "repeated movies didn't match what was expected";
  }
}
