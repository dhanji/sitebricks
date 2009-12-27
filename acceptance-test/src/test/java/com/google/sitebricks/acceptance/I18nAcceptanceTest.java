package com.google.sitebricks.acceptance;

import com.google.inject.internal.ImmutableMap;
import com.google.sitebricks.acceptance.page.I18nPage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class I18nAcceptanceTest {

  public void shouldRenderLocalizedDynamicTextEnUs() {
    I18nPage page = I18nPage.openWithHeaders(ImmutableMap.of("Accept-Language", "en-US"));

    assert page.hasHelloTo(I18nPage.NAME) : "Did not generate dynamic text from i18n message set";
  }

  public void shouldRenderLocalizedDynamicTextFrCa() {
    I18nPage page = I18nPage.openWithHeaders(ImmutableMap.of("Accept-Language", "fr-CA"));

    assert page.hasBonjourTo(I18nPage.NAME) : "Did not generate dynamic text from i18n message set";
  }
}
