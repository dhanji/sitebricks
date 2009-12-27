package com.google.sitebricks.acceptance.page;

import com.google.inject.Guice;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.client.Web;
import com.google.sitebricks.client.transport.Text;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import java.util.Map;

public class I18nPage {
  private final String content;
  public static final String NAME = "Dhanji";

  public I18nPage(String content) {
    try {
      this.content = DocumentHelper.parseText(content)
          .selectSingleNode("//span['localizedMessage'][1]")
          .getText();
    } catch (DocumentException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean hasHelloTo(String name) {
    return content.trim().equals("Hello there " + name + "!");
  }

  public boolean hasBonjourTo(String name) {
    return content.trim().equals("Bonjour misieu " + name + "!");
  }

  public static I18nPage openWithHeaders(Map<String, String> headers) {
    String content = Guice.createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/i18n?name=" + NAME, headers)
        .transports(String.class)
        .over(Text.class)
        .get()
        .toString();

    return new I18nPage(content);
  }
}
