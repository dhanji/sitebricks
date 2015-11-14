package com.google.sitebricks.acceptance;

import com.google.sitebricks.acceptance.page.EmbeddedRepeatPage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.apache.commons.collections.CollectionUtils;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * author: Martins Barinskis (martins.barinskis@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class EmbeddedRepeatTest extends SitebricksJettyAcceptanceTest {

    public void shouldRenderPassedParameterInEveryItem() {
        List<String> expectedContinents = Arrays.asList("Continent Europe", "Continent Asia", "Continent North America");

        List<String> expectedCountries = Arrays.asList("Country Germany", "Country United Kingdom", "Country France",
                                                        "Country Japan", "Country China",
                                                        "Country United States", "Country Canada");

        WebDriver driver = AcceptanceTest.createWebDriver();
        EmbeddedRepeatPage page = EmbeddedRepeatPage.open(driver);

        List<String> continents = page.getContinents();
        List<String> countries = page.getCountries();

        assert CollectionUtils.isEqualCollection(expectedContinents, continents)
                : "Repeated continent names didn't match what was expected";
        assert CollectionUtils.isEqualCollection(expectedCountries, countries)
                : "Repeated country names didn't match what was expected";

    }

}
