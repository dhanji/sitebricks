package com.google.sitebricks.mail.imap;

import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNull;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class SearchResultExtractorTest {
  @Test
  public void testExtractNoResults() throws Exception {
    List<String> messages = ImmutableList.of(
        "* search"
    );

    assertNull(new SearchResultExtractor().extract(messages));
  }

  @Test
  public void testExtract() throws Exception {
    List<String> messages = ImmutableList.of(
        "* search 123 3 6 4 -10"
    );

    assertEquals(Arrays.asList(123, 3, 6, 4, -10), new SearchResultExtractor().extract(messages));
  }
}
