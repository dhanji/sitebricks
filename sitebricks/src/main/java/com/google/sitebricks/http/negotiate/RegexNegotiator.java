package com.google.sitebricks.http.negotiate;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Map;

/**
 * ContentNegotiator that supports one regex match value, for example
 * {@literal @}Accept("(xml|text)/.*") will match any incoming request with an
 * HTTP Accept header "text/*" and {@literal @}Referer("(google|yahoo|bing)\\.com") will
 * match requests with HTTP Referer headers from google, yahoo, or bing
 */
public class RegexNegotiator implements ContentNegotiator {

  public boolean shouldCall(Map<String, String> negotiations, HttpServletRequest request) {
    for (Map.Entry<String, String> negotiate : negotiations.entrySet()) {

      @SuppressWarnings("unchecked") // Guaranteed by servlet spec.
      Enumeration<String> headerValues = request.getHeaders(negotiate.getKey());
      String match = negotiate.getValue();

      boolean shouldFire = false;       // Guaranteed never to throw NPE
      while (headerValues.hasMoreElements()) {
        String value = headerValues.nextElement();

        shouldFire |= value.matches(match);
        
        for (String val: value.split(",[ ]*")) {
         shouldFire |= val.matches(match);
        }
      }
      if (!shouldFire) {
        return false;
      }
    }
    return true;
  }
}
