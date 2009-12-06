package com.google.sitebricks.client;

import com.google.inject.Guice;
import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class WebClientIntegrationTest {

  @Test
  public final void simpleJsonGetFromTwitter() {
    Web web = Guice.createInjector().getInstance(Web.class);

    WebClient<String> webClient = web.clientOf("http://twitter.com/statuses/public_timeline.json")
        .transports(String.class)
        .over(Json.class);

    final WebResponse response = webClient.get();

    assert response.toString().contains("statuses");
  }
}
