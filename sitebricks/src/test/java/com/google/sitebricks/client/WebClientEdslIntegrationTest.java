package com.google.sitebricks.client;

import com.google.inject.Guice;
import com.google.sitebricks.client.transport.Text;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class WebClientEdslIntegrationTest {

//  @Test DISABLED
  public final void edslForBinding() {
    Web resource = Guice.createInjector().getInstance(Web.class);

    WebClient<String> webClient = resource.clientOf("http://google.com")
        .transports(String.class)
        .over(Text.class);

    final WebResponse response = webClient.get();

    final String responseAsString = response.toString();

    assert responseAsString.contains("Google");
  }
}
