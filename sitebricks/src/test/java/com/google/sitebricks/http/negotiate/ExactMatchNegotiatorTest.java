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

/**
 * Unit test for ExactMatchNegotiator
 */
public class ExactMatchNegotiatorTest {
  private static final String HEADERS_AND_NEGOTIATIONS = "HEADERS_NEGS";

  @DataProvider(name = HEADERS_AND_NEGOTIATIONS)
  public Object[][] headersAndNegotiations() {
    return new Object[][] {
        { ImmutableMap.of(), Multimaps.forMap(ImmutableMap.of()), true },

        // negotation, but no headers matching
        { ImmutableMap.of("Accept", "thing"), Multimaps.forMap(ImmutableMap.of()), false },

        // headers but no negs
        { ImmutableMap.of(), Multimaps.forMap(ImmutableMap.of("Accept", "image/png")), true },

        // disjoint set of headers and negs
        { ImmutableMap.of("Accept", "thing"), Multimaps.forMap(ImmutableMap.of("Content-Accept", "image/png")), false },

        // Non matching set of the same header
        { ImmutableMap.of("Accept", "thing"), Multimaps.forMap(ImmutableMap.of("Accept", "image/png")), false },

        // Matching set of the same header
        { ImmutableMap.of("Accept", "thing"), Multimaps.forMap(ImmutableMap.of("Accept", "thing")), true },

        // Matching set of the same header, but case-mismatch
        { ImmutableMap.of("Accept", "thing"), Multimaps.forMap(ImmutableMap.of("Accept", "THING")), false },

        // Multiple header values, one matches
        { ImmutableMap.of("Accept", "thing"), Multimaps.forMap(ImmutableMap.of("Accept", "nonthing, thing")), true },

        // Multiple header values, one matches, different order
        { ImmutableMap.of("Accept", "thing"), Multimaps.forMap(ImmutableMap.of("Accept", "thing, hno, asdo")), true },

        // Multiple header values, some match, some don't
        { ImmutableMap.of("Accept", "thing", "Content-Accept", "nothing"),
            Multimaps.forMap(ImmutableMap.of("Accept", "thing, hno, asdo")),
            false },

        // Multiple header values, some match, some don't, but all passes
        { ImmutableMap.of("Accept", "thing", "Content-Accept", "nothing"),
            Multimaps.forMap(ImmutableMap.of("Accept", "thing, hno, asdo", "Content-Accept", "aisdjf, nothing, aiosjdf")),
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

    assert shouldPass == new ExactMatchNegotiator().shouldCall(negotiations, request);
  }
}
