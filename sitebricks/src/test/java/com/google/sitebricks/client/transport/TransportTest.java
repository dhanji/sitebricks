package com.google.sitebricks.client.transport;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Unit test for the various transports supported out of the box.
 */
public class TransportTest {
  private static final String TEXT_DATA = "text";

  @DataProvider(name = TEXT_DATA)
  public Object[][] textData() {
    return new Object[][] {
        { "Hello there 2793847!@(*&#(!*@&#ASDJFA <SAAC<>M??X{." },
        { "\\ \n \n \t \n \0 oaijsdfoijasdoifjao;sidjf19823749872w34*@(#$*&BMBMB" },
        { "19827981273981723981729387192837912873912873" },
        { "                                                      " },
        { getClass().toString() },
        { System.getProperties().toString()  },
    };
  }

  @Test(dataProvider = TEXT_DATA)
  public final void textTransport(String data) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    new Text().out(out, String.class, data);

    String in = new Text().in(new ByteArrayInputStream(out.toByteArray()), String.class);

    assert data.equals(in) : "Text transport was not balanced";
  }
}
