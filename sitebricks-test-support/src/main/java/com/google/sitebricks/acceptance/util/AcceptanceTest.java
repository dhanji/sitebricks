package com.google.sitebricks.acceptance.util;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * @author Tom Wilson (tom@tomwilson.name)
 */
public class AcceptanceTest {
  public static final String SUITE = "acceptance";

  public static WebDriver createWebDriver() {
    return new HtmlUnitDriver();
  }

  public static String baseUrl() {
    return Jetty.baseUrl();
  }
}
