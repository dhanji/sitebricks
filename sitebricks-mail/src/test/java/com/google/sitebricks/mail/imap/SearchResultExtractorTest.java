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
  @Test
  public void testExtractLonger() throws Exception {
    List<String> messages = ImmutableList.of(
        "* search  42187 275543 287963 298411 298834 299843 305799 306131 319620 323938 328364 329370 329566 329599 331454 332401 338293 339646 341368 342169 342969 343541 344924 345047 345730 348037 349247 351914 351941 351945 352041 352461 352462",
        "* Ok SEARCH completed (Success)"
    );

    String thing = "[42187 275543 287963 298411 298834 299843 305799 306131 319620 323938 328364 329370 329566 329599 331454 332401 338293 339646 341368 342169 342969 343541 344924 345047 345730 348037 349247 351914 351941 351945 352041 352461 352462]".replace(" ", ", ");


    assertEquals(thing, new SearchResultExtractor().extract(messages).toString());
  }
}
