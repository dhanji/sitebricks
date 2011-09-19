package com.google.sitebricks.mail.imap;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class ParsingTest {
  @Test
  public final void normalizeDate() {
    assertEquals("Thu, 07 Apr 2011 04:41:42 -0700",
        Parsing.normalizeDateToken("Thu, 07 Apr 2011 04:41:42 -0700 (PDT)"));
  }

  @Test
  public final void addressParsing() {
    assertEquals(
        Parsing.readAddress(Parsing.tokenize("(\"Dhanji Prasanna\" NIL \"dhanji\" \"gmail.com\")")),
        "\"Dhanji Prasanna\" dhanji@gmail.com");

    assertEquals(
        Parsing.readAddress(Parsing.tokenize("(Dhanji\\ Prasanna NIL \"dhanji\" \"gmail.com\")")),
        "\"Dhanji Prasanna\" dhanji@gmail.com");

    assertEquals(
        Parsing.readAddress(Parsing.tokenize(
            "(\"<newsletter=pukaraestate.com.au@mail85.us1.rsgsv.net>\" NIL \"\\\"Pukara Estate\\\"\" NIL)")),
        "\"<newsletter=pukaraestate.com.au@mail85.us1.rsgsv.net>\" \\\"Pukara Estate\\\"@null");
  }
}
