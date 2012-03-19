package com.google.sitebricks.mail.imap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;


/**
 * Test StoreFlagsResponseExtractorTest and StoreLabelsResponseExtractorTest classes.
 *
 * @author jochen@pedesis.org (Jochen Bekmann)
 */
public class StoreResponseExtractorTest {
  private static final List<String> BAD_RESPONSE = ImmutableList.of("66 BAD stupid stuff.");
  private static final List<String> NO_RESPONSE = ImmutableList.of("66 NO meh, you can't");
  private StoreFlagsResponseExtractor flagsExtractor;
  private StoreLabelsResponseExtractor labelsExtractor;

  @BeforeTest
  public final void setup() {
    flagsExtractor = new StoreFlagsResponseExtractor();
    labelsExtractor = new StoreLabelsResponseExtractor();
  }

  @Test
  public final void testBadResponse() {
    try {
      flagsExtractor.extract(BAD_RESPONSE);
      fail("Expected BAD response to fail.");
    } catch (ExtractionException ee) {
      // expected.
    }
    try {
      labelsExtractor.extract(BAD_RESPONSE);
      fail("Expected BAD response to fail.");
    } catch (ExtractionException ee) {
      // expected.
    }
  }

  @Test
  public final void testNoResponse() {
    try {
      flagsExtractor.extract(NO_RESPONSE);
      fail("Expected NO response to fail.");
    } catch (ExtractionException ee) {
      // expected.
    }
    try {
      labelsExtractor.extract(NO_RESPONSE);
      fail("Expected NO response to fail.");
    } catch (ExtractionException ee) {
      // expected.
    }
  }

  @Test
  public final void testOkFlagsEmptyResponse() throws ExtractionException {
    List<String> OK_RESPONSE = ImmutableList.of("* 4 FETCH (FLAGS () UID 6)", "66 OK STORE_FLAGS completed");
    assertEquals(flagsExtractor.extract(OK_RESPONSE), ImmutableSet.of());
  }

  @Test
  public final void testOkFlagsWithResponse() throws ExtractionException {
    List<String> OK_RESPONSE = ImmutableList.of("* 4 FETCH (FLAGS (\\Deleted \\Flagged " +
        "\\Seen) UID 6)", "66 OK STORE_FLAGS completed");
    assertEquals(flagsExtractor.extract(OK_RESPONSE), ImmutableSet.of(Flag.SEEN, Flag.DELETED, Flag.FLAGGED));
  }

  @Test
  public final void testFlagsStingifier() {
    Set<Flag> testSet = ImmutableSet.of(Flag.SEEN, Flag.DELETED, Flag.FLAGGED);
    assertEquals(Flag.toImap(testSet), "FLAGS (\\Seen \\Deleted \\Flagged)");
  }

  @Test
  public final void testOkLabelsEmptyResponse() throws ExtractionException {
    List<String> OK_RESPONSE = ImmutableList.of("* 4 FETCH (X-GM-LABELS () UID 6)", "66 OK STORE_FLAGS completed");
    assertEquals(labelsExtractor.extract(OK_RESPONSE), ImmutableSet.of());
  }

  @Test
  public final void testOkLabelsWithResponse() throws ExtractionException {
    List<String> OK_RESPONSE = ImmutableList.of("* 4 FETCH (X-GM-LABELS (\\Foo \\Bar " +
        "Baz) UID 6)", "66 OK all good, captain.");
    assertEquals(labelsExtractor.extract(OK_RESPONSE), ImmutableSet.of("Baz", "\\Foo", "\\Bar"));
  }

  @Test
  public final void testOkQuotedLabelsWithResponse() throws ExtractionException {
    List<String> OK_RESPONSE = ImmutableList.of("* 4 FETCH (X-GM-LABELS (\"\\\\Foo\" \"\\\\Bar\" " +
        "\"Baz\") UID 6)", "66 OK all good, captain.");
    assertEquals(labelsExtractor.extract(OK_RESPONSE), ImmutableSet.of("\"Baz\"", "\"\\Foo\"", "\"\\Bar\""));
  }
}
