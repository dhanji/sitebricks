package com.google.sitebricks.http.negotiate;

import com.google.common.collect.Multimap;
import com.google.sitebricks.headless.Request;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * ContentNegotiator that supports one regex match value, for example
 * {@literal @}Accept("(xml|text)/.*") will match any incoming request with an
 * HTTP Accept header "text/*" and {@literal @}Referer("(google|yahoo|bing)\\.com") will
 * match requests with HTTP Referer headers from google, yahoo, or bing
 */
public class RegexNegotiator implements ContentNegotiator {

  public boolean shouldCall(Map<String, String> negotiations, Request request) {
    Multimap<String, String> headers = request.headers();
    for (Map.Entry<String, String> negotiate : negotiations.entrySet()) {

      Collection<String> collectionOfHeader = headers.get(negotiate.getKey());
      if (null == collectionOfHeader)
        continue;
      Iterator<String> headerValues = collectionOfHeader.iterator();
      String match = negotiate.getValue();

      boolean shouldFire = false;       // Guaranteed never to throw NPE
      while (headerValues.hasNext()) {
        String value = headerValues.next();

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
