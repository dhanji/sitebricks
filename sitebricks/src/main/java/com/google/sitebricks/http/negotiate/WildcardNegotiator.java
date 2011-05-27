package com.google.sitebricks.http.negotiate;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.sitebricks.headless.Request;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ContentNegotiator that supports comma separated and wildcard matches in Accept header style
 * for example, {@literal @}Accept("text/html, text/plain") will match an incoming request
 * with HTTP Accept header "text/*" and {@literal @}Accept("text/*") will match incoming
 * request with headers "Accept: text/html" or "Accept: text/plain"
 *
 * Notes:
 *   Wildcard for subtypes such as "text/*, image/*" are supported but there is no
 *   wildcard matching on the main media types.   Negotiating on other HTTP request
 *   headers where "/*" might be useful is currently undefined.
 *
 *
 */
public class WildcardNegotiator implements ContentNegotiator {
  // Lifted TOKEN, TYPE_PATTERN  from com.google.gdata.util

  private static String TOKEN =
    "[\\p{ASCII}&&[^\\p{Cntrl} ;/=\\[\\]\\(\\)\\<\\>\\@\\,\\:\\\"\\?\\=]]+";

  private static Pattern TYPE_PATTERN = Pattern.compile(
    "(" + TOKEN + ")" +         // mediatype (G1)
    "/" +                       // separator
    "(" + TOKEN + ")" +         // subtype (G2)
    "\\s*(.*)\\s*", Pattern.DOTALL);

  private HashMultimap<String, String> createMultimatch(List<String> matchlist) {
    HashMultimap<String, String> multimatch = HashMultimap.create();
      for (String m : matchlist) {
        Matcher mediaType = TYPE_PATTERN.matcher(m);

        if (mediaType.matches()) {
          String type = mediaType.group(1).toLowerCase();
          String subtype = mediaType.group(2).toLowerCase();
          multimatch.put(type, subtype);
        }
      }
    return multimatch;
  }

  public boolean shouldCall(Map<String, String> negotiations, Request request) {
    Multimap<String, String> headers = request.headers();
    for (Map.Entry<String, String> negotiate : negotiations.entrySet()) {

      Collection<String> collectionOfHeader = headers.get(negotiate.getKey());
      if (null == collectionOfHeader)
        continue;
      Iterator<String> headerValues = collectionOfHeader.iterator();
      boolean shouldFire = false;

      List<String> matches = Arrays.asList(negotiate.getValue().split(",[ ]*"));
      HashMultimap<String,String> mediaMatches = createMultimatch(matches);

      while (headerValues.hasNext()) {
        String value = headerValues.next();

        List<String> values = Arrays.asList(value.split(",[ ]*"));
        HashMultimap<String,String> mediaValues = createMultimatch(values);

        if (!mediaMatches.isEmpty()) {
          Set<String> typeIntersection = Sets.intersection(mediaMatches.keySet(), mediaValues.keySet());
          if (typeIntersection.isEmpty()) {
            shouldFire |= typeIntersection.isEmpty();
          } else {
            for (String mediaType: typeIntersection) {
              Set<String> subtypeMatches = mediaMatches.get(mediaType);
              Set<String> subtypeValues = mediaValues.get(mediaType);
              
              shouldFire |= (subtypeMatches.contains("*")
                  || subtypeValues.contains("*")
                  || !Sets.intersection(subtypeMatches, subtypeValues).isEmpty());
            }
          }
        } else {
          shouldFire |= !(Collections.disjoint(Arrays.asList(value.split(",[ ]*")), matches));
        }
      }
      if (!shouldFire) {
        return false;
      }
    }
    return true;
  }
}

// TODO - http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
//      Accept: text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5

// TODO - other headers with slashes (but not signifying media types)
