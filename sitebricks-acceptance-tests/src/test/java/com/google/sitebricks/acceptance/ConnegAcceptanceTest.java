package com.google.sitebricks.acceptance;

import com.google.common.collect.ImmutableMap;
import com.google.sitebricks.acceptance.page.ConnegPage;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class ConnegAcceptanceTest {

  public void shouldReturnGifByDefault() {
    ConnegPage page = ConnegPage.openWithHeaders(ImmutableMap.<String, String>of());

    assert page.hasContent("GIF")
        : "Did not have gif!";
  }

  public void shouldReturnJpeg() {
    ConnegPage page = ConnegPage.openWithHeaders(ImmutableMap.of("Accept", "image/jpeg"));

    assert page.hasContent("JPEG")
        : "Did not have jpeg!";
  }

  public void shouldReturnPng() {
    ConnegPage page = ConnegPage.openWithHeaders(ImmutableMap.of("Accept", "image/png"));

    assert page.hasContent("PNG")
        : "Did not have jpeg!";
  }

  public void shouldReturnPngOrJpeg() {
    ConnegPage page = ConnegPage.openWithHeaders(ImmutableMap.of("Accept", "image/png, image/jpeg"));

    assert page.hasContent("PNG") || page.hasContent("JPEG")
        : "Did not have jpeg!";
  }
}
