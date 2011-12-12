package com.google.sitebricks.http.negotiate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Enumeration;
import java.util.Map;

import static org.easymock.EasyMock.createMock;

public class RegexNegotiatorTest {
  private static final String HEADERS_AND_NEGOTIATIONS = "HEADERS_NEGS";

  @DataProvider(name = HEADERS_AND_NEGOTIATIONS)
  public Object[][] headersAndNegotiations() {
    return new Object[][] {
        { ImmutableMap.of(), Multimaps.forMap(ImmutableMap.of()), true },

        // negotation, but no headers matching
        { ImmutableMap.of("Accept", "thing"), Multimaps.forMap(ImmutableMap.of()), false },
        { ImmutableMap.of("Accept", "thing, other-thing"), Multimaps.forMap(ImmutableMap.of()), false },

        // headers but no negs
        { ImmutableMap.of(), Multimaps.forMap(ImmutableMap.of("Accept", "image/png")), true },

        // disjoint set of headers and negs
        { ImmutableMap.of("Accept", "thing"), Multimaps.forMap(ImmutableMap.of("Content-Accept", "image/png")), false },
        { ImmutableMap.of("Accept", ".*thing.*"), Multimaps.forMap(ImmutableMap.of("Content-Accept", "image/png")), false },

        // Non matching set of the same header
        { ImmutableMap.of("Accept", ".*thing.*"), Multimaps.forMap(ImmutableMap.of("Accept", "image/png")), false },

        // Matching set of the same header
        { ImmutableMap.of("Accept", ".*thing.*"), Multimaps.forMap(ImmutableMap.of("Accept", "thing")), true },

        // Matching set of the same header, but different cases
        { ImmutableMap.of("Accept", ".*thing.*"), Multimaps.forMap(ImmutableMap.of("Accept", "THING")), false },

        // Multiple header values, one matches
        { ImmutableMap.of("Accept", ".*thing.*"), Multimaps.forMap(ImmutableMap.of
          ("Accept", "nonthing, thing")), true },
        { ImmutableMap.of("Referer", ".*(google|yahoo|bing)\\.com.*"), Multimaps.forMap(ImmutableMap.of
          ("Accept", "text/*, nonthing", "Referer", "http://google.com/")), true },
        { ImmutableMap.of("Accept", ".*text/.*"), Multimaps.forMap(ImmutableMap.of
          ("Accept", "nonthing, text/plain", "Referer", "http://google.com/")), true },

        // Multiple header values, one matches, different order
        { ImmutableMap.of("Accept", ".*thing.*"), Multimaps.forMap(ImmutableMap.of("Accept", "thing, hno, asdo")), true },

        // Multiple header values, some match, some don't
        { ImmutableMap.of("Accept", "thing", "Content-Accept", "nothing"), Multimaps.forMap(ImmutableMap.of("Accept", "thing, hno, asdo")), false },

        // Multiple header values, some match, some don't, but all passes
        { ImmutableMap.of("Accept", ".*thing.*", "Content-Accept", ".*nothing.*"), Multimaps.forMap(ImmutableMap.of("Accept", "thing, hno, asdo", "Content-Accept", "aisdjf, nothing, aiosjdf")), true },




        // from the wild
        { ImmutableMap.of("Accept", ".*text/(\\*|html).*"), Multimaps.forMap(ImmutableMap.of
          ("Accept", "text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5")),
          true },
        { ImmutableMap.of("Accept", ".*/xml.*"), Multimaps.forMap(ImmutableMap.of
          ("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")),
          true },
        { ImmutableMap.of("Accept", ".*(application|text)/.*xml.*"), Multimaps.forMap(ImmutableMap.of
          ("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")),
          true },
        { ImmutableMap.of("Accept", ".*(application|text)/.*xml.*"), Multimaps.forMap(ImmutableMap.of
          ("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9, text/plain;q=0.8,image/png,*/*;q=0.5")),
          true },
        { ImmutableMap.of("Accept", ".*application/.*(silverlight|flash|shockwave).*"), Multimaps.forMap(ImmutableMap.of
        ("Accept", "image/gif, image/jpeg, image/pjpeg, application/x-ms-application, application/msword, " +
          "application/vnd.ms-xpsdocument, application/xaml+xml, application/x-ms-xbap, application/x-shockwave-flash, " +
          "application/x-silverlight-2-b2, application/x-silverlight, application/vnd.ms-excel, application/vnd.ms-powerpoint, */*")),
        true },




    };
  }


  @Test(dataProvider = HEADERS_AND_NEGOTIATIONS)
  public final void variousHeadersAndNegotiations(Map<String, String> negotiations,
                                                  final Multimap<String, String> headers,
                                                  boolean shouldPass) {
    HttpServletRequest request = new HttpServletRequestWrapper(createMock(HttpServletRequest.class)) {
      @Override
      public Enumeration getHeaders(String name) {
        return Iterators.asEnumeration(headers.get(name).iterator());
      }
    };

    assert shouldPass == new RegexNegotiator().shouldCall(negotiations, request);
  }
}