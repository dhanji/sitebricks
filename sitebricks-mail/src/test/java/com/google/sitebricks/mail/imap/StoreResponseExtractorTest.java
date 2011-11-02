package com.google.sitebricks.mail.imap;

import com.google.common.collect.ImmutableList;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.mail.Flags;
import javax.xml.stream.events.EndElement;
import java.util.EnumSet;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;


/**
 * @author jochen@pedesis.org (Jochen Bekmann)
 */
public class StoreResponseExtractorTest {
  private static final List<String> BAD_RESPONSE = ImmutableList.of("66 BAD stupid stuff.");
  private static final List<String> NO_RESPONSE = ImmutableList.of("66 NO meh, you can't");
  private StoreResponseExtractor extractor;

  @BeforeTest
  public final void setup() {
    extractor = new StoreResponseExtractor();
  }

  @Test
  public final void testBadResponse() {
    try {
      extractor.extract(BAD_RESPONSE);
      fail("Expected BAD response to fail.");
    }
    catch (ExtractionException ee) {
      // expected.
    }
  }

  @Test
  public final void testNoResponse() {
    try {
      extractor.extract(NO_RESPONSE);
      fail("Expected NO response to fail.");
    }
    catch (ExtractionException ee) {
      // expected.
    }
  }

  @Test
  public final void testOkEmptyResponse() throws ExtractionException {
    List<String> OK_RESPONSE = ImmutableList.of("* 4 FETCH (FLAGS () UID 6)",  "66 OK STORE completed");
    assertEquals(EnumSet.noneOf(Flag.class), extractor.extract(OK_RESPONSE));
  }

  @Test
  public final void testOkWithResponse() throws ExtractionException {
    List<String> OK_RESPONSE = ImmutableList.of("* 4 FETCH (FLAGS (\\Deleted \\Flagged " +
        "\\Seen) UID 6)",  "66 OK STORE completed");
    assertEquals(EnumSet.of(Flag.SEEN, Flag.DELETED, Flag.FLAGGED), extractor.extract(OK_RESPONSE));
  }

  @Test
  public final void testStingifier() {
    EnumSet<Flag> testSet = EnumSet.of(Flag.SEEN, Flag.DELETED, Flag.FLAGGED);
    assertEquals("FLAGS (\\seen \\deleted \\flagged)", Flag.toImap(testSet));
  }

}
