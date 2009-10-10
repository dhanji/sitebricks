package com.google.sitebricks.acceptance;

import com.google.sitebricks.acceptance.page.RepeatPage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.apache.commons.collections.CollectionUtils;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Tom Wilson (tom@tomwilson.name)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class RepeatAcceptanceTest {

  public void shouldRepeatItemsFromCollection() {
    WebDriver driver = AcceptanceTest.createWebDriver();
    RepeatPage page = RepeatPage.open(driver);

    List<String> expectedNames = Arrays.asList("Josh", "Dhanji", "Jody", "Iron Man");
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
