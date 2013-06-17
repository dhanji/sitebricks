package com.google.sitebricks.acceptance.page;

import com.google.inject.Guice;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.client.Web;
import com.google.sitebricks.client.transport.Text;
import java.util.Map;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

/**
 * Page object that wraps the content negotiation page.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class ConnegPage {
  private String content;

  private ConnegPage(String content) {
    try {
      this.content = DocumentHelper.parseText(content)
          .selectSingleNode("//div['content'][1]")
          .getText();
    } catch (DocumentException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean hasContent(String content) {
    return this.content.trim().equals(content);
  }

  public static ConnegPage openWithHeaders(Map<String, String> headers) {
    String content = Guice.createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.baseUrl() + "/conneg", headers)
        .transports(String.class)
        .over(Text.class)
        .get()
        .toString();

    return new ConnegPage(content);
  }
}
