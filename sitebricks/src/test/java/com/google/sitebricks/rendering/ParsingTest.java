package com.google.sitebricks.rendering;

import com.google.sitebricks.compiler.Parsing;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class ParsingTest {
  private static final String XML_AND_FLAT_TEMPLATES = "XMLandFlats";

  @DataProvider(name = XML_AND_FLAT_TEMPLATES)
  public Object[][] get() {
    return new Object[][]{
        {"   @Meta() Hello world!", false},
        {"   <xml>@Meta() Hello world!</xml>", true},
        {"   @Meta() <Hello> world!</Hello>", false},
        {" <X  @Meta() Hello world!", true},
        {"  \n\n\n @Meta() Hello world!", false},
        {"  \n\n\n @Meta() Hello world!", false},
        {"  \n\t @Meta() Hello world!", false},
        {"  \n\r @Meta() Hello world!", false},
        {"  \n\r @Metas() Hello world!", true},
        {" \t \n\r @Meta) Hello world!", true},
        {" \t \n\r @Meta@Meta Hello world!", true},
        {" \t \n\r @@Meta Hello world!", true},
        {" \t \n\r @\n@Meta Hello world!", true},
        {"      \n\r @Meta Hello world!", false},
    };
  }

  @Test(dataProvider = XML_AND_FLAT_TEMPLATES)
  public final void isXmlTemplateOrNot(final String template, boolean is) {
    assert is == Parsing.treatAsXml(template);
  }

  /**
   * URI(Uniform Resource identifiers)
   * http://www.ietf.org/rfc/rfc2396.txt
   *
   * Huge thanks for this contribution to: qizhanming
   */
  @Test
  public void testIsValidURI() {
    String uri = null;
    assert !Parsing.isValidURI(uri);
    /*
    * first, test right URI
    */

    uri = "ftp://ftp.is.co.za/rfc/rfc1808.txt";
    //-- ftp scheme for File Transfer Protocol services
    assert Parsing.isValidURI(uri);

    uri = "gopher://spinaltap.micro.umn.edu/00/Weather/California/Los%20Angeles";
    //-- gopher scheme for Gopher and Gopher+ Protocol services
    assert Parsing.isValidURI(uri);

    uri = "http://www.math.uio.no/faq/compression-faq/part1.html";
    //-- http scheme for Hypertext Transfer Protocol services
    assert Parsing.isValidURI(uri);

    uri = "mailto:mduerst@ifi.unizh.ch";
    //-- mailto scheme for electronic mail addresses
    assert Parsing.isValidURI(uri);

    uri = "news:comp.infosystems.www.servers.unix";
    //-- news scheme for USENET news groups and articles
    assert Parsing.isValidURI(uri);

    uri = "telnet://melvyl.ucop.edu/";
    //-- telnet scheme for interactive services via the TELNET Protocol
    assert Parsing.isValidURI(uri);
  }
}
