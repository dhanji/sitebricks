package com.google.sitebricks.http.negotiate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.sitebricks.TestRequestCreator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Enumeration;
import java.util.Map;

import static org.easymock.EasyMock.createMock;

public class WildcardNegotiatorTest {
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
        { ImmutableMap.of("Accept", "thing, text/something, image/gif"), Multimaps.forMap(ImmutableMap.of("Content-Accept", "image/png")), false },

        // Non matching set of the same header
        { ImmutableMap.of("Accept", "thing"), Multimaps.forMap(ImmutableMap.of("Accept", "image/png")), false },
        { ImmutableMap.of("Accept", "thing, text/something, image/gif"), Multimaps.forMap(ImmutableMap.of("Accept", "image/png")), false },

        // Matching set of the same header
        { ImmutableMap.of("Accept", "thing"), Multimaps.forMap(ImmutableMap.of("Accept", "thing")), true },
        { ImmutableMap.of("Accept", "thing, other-thing"), Multimaps.forMap(ImmutableMap.of("Accept", "thing")), true },
        { ImmutableMap.of("Accept", "thing, image/*"), Multimaps.forMap(ImmutableMap.of("Accept", "thing, image/tiff")), true },
        { ImmutableMap.of("Accept", "thing, other-thing, image/tiff"), Multimaps.forMap(ImmutableMap.of("Accept", "thing, image/*")), true },

        // Matching set of the same header, but different cases
        { ImmutableMap.of("Accept", "thing"), Multimaps.forMap(ImmutableMap.of("Accept", "THING")), false },
        { ImmutableMap.of("Accept", "thing, other-thing"), Multimaps.forMap(ImmutableMap.of("Accept", "THING, OTHER-THING")), false },

        // Matching set of the same header, match different cases in mediatypes (according to spec)
        { ImmutableMap.of("Accept", "thing, text/WhAtEveR, FOo/bar"), Multimaps.forMap(ImmutableMap.of("Accept", "THING, OTHER-THING, text/whatever, foo/bar")), true },
        { ImmutableMap.of("Accept", "this/THAT, FOO/bar, whoa/*"), Multimaps.forMap(ImmutableMap.of("Accept", "NOTHING, whoa/PLAIN, THIS/that, foo/BAR")), true },

        // Multiple header values, one matches
        { ImmutableMap.of("Accept", "thing"), Multimaps.forMap(ImmutableMap.of("Accept", "nonthing, thing")), true },
        { ImmutableMap.of("Accept", "thing, other-thing"), Multimaps.forMap(ImmutableMap.of("Accept", "nonthing, thing")), true },

        // Multiple header values, non media-type one match
        { ImmutableMap.of("Accept", "thing, text/*"), Multimaps.forMap(ImmutableMap.of("Accept", "nonthing, thing")), true },
        { ImmutableMap.of("Accept", "thing, nonthing"), Multimaps.forMap(ImmutableMap.of("Accept", "text/*, nonthing")), true },

        // Multiple header values, one matches, different order
        { ImmutableMap.of("Accept", "thing"), Multimaps.forMap(ImmutableMap.of("Accept", "thing, hno, asdo")), true },
        { ImmutableMap.of("Accept", "another-thing, thing"), Multimaps.forMap(ImmutableMap.of("Accept", "thing, hno, asdo")), true },

        // Multiple header values, some match, some don't
        { ImmutableMap.of("Accept", "thing", "Content-Accept", "nothing"), Multimaps.forMap(ImmutableMap.of("Accept", "thing, hno, asdo")), false },
        { ImmutableMap.of("Accept", "thing, another-thing/coming", "Content-Accept", "nothing"), Multimaps.forMap(ImmutableMap.of("Accept", "thing, hno, asdo")), false },
        { ImmutableMap.of("Accept", "thing", "Content-Accept", "nothing"), Multimaps.forMap(ImmutableMap.of("Accept", "thing, hno, asdo")), false },
        { ImmutableMap.of("Accept", "thing, text/*, another-thing/coming", "Content-Accept", "nothing"), Multimaps.forMap(ImmutableMap.of("Accept", "thing, image/*, hno, asdo")), false },

        // Multiple header values, some match, some don't, but all passes
        { ImmutableMap.of("Accept", "thing", "Content-Accept", "nothing"), Multimaps.forMap(ImmutableMap.of("Accept", "thing, hno, asdo", "Content-Accept", "aisdjf, nothing, aiosjdf")), true },
        { ImmutableMap.of("Accept", "thing, another-thing, something", "Content-Accept", "nothing, kaupthing, clothing"), Multimaps.forMap(ImmutableMap.of("Accept", "thing, hno, asdo", "Content-Accept", "aisdjf, nothing, aiosjdf")), true },

        // Multiple header values, one matches wildcard neg subtype
        { ImmutableMap.of("Accept", "thing, text/foo, text/*"), Multimaps.forMap(ImmutableMap.of("Accept", "nonthing, text/plain")), true },
        { ImmutableMap.of("Accept", "image/*, text/plain, text/*"), Multimaps.forMap(ImmutableMap.of("Accept", "nonthing, text/html, image/tiff")), true },

        // Multiple header values, one with wildcard subtype, matches
        { ImmutableMap.of("Accept", "thing, text/foo, text/plain"), Multimaps.forMap(ImmutableMap.of("Accept", "nonthing, text/*")), true },
        { ImmutableMap.of("Accept", "image/png, text/plain, text/ual"), Multimaps.forMap(ImmutableMap.of("Accept", "nonthing, text/html, image/*")), true },


        // from the wild
        { ImmutableMap.of("Accept", "text/html"), Multimaps.forMap(ImmutableMap.of
          ("Accept", "text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;level=2;q=0.4, */*;q=0.5")),
          true },
        { ImmutableMap.of("Accept", "application/xhtml+xml, application/xml"), Multimaps.forMap(ImmutableMap.of
          ("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")),
          true },
        { ImmutableMap.of("Accept", "application/xhtml+xml, application/xml"), Multimaps.forMap(ImmutableMap.of
          ("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")),
          true },
        { ImmutableMap.of("Accept", "application/xhtml+xml, application/xml"), Multimaps.forMap(ImmutableMap.of
          ("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9, text/plain;q=0.8,image/png,*/*;q=0.5")),
          true },
        { ImmutableMap.of("Accept", "application/x-silverlight, application/ho-ho-ho"), Multimaps.forMap(ImmutableMap.of
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

      @Override public Enumeration getHeaderNames() {
        return Iterators.asEnumeration(headers.keys().iterator());
      }
    };

    assert shouldPass == new WildcardNegotiator().shouldCall(negotiations, TestRequestCreator.from(
        request, null));
  }
}