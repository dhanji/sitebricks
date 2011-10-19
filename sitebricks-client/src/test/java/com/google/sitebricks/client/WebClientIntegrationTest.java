package com.google.sitebricks.client;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.sitebricks.client.transport.Text;
import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @Expensive
 */
public class WebClientIntegrationTest {

//  @Test Disabled as you need to be online for this to work
  public final void simpleJsonGetFromTwitter() {
    Web web = Guice.createInjector().getInstance(Web.class);

    WebClient<String> webClient = web.clientOf("http://twitter.com/statuses/public_timeline.json")
        .transports(String.class)
        .over(Text.class);

    final WebResponse response = webClient.get();

    assert response.toString().contains("statuses");
  }

//  @Test Disabled as you need to be online for this to work
  public void simpleJsonGetFromTwitterUsingCustomJdkClient() {

    Web web = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        // we need to override the standard Web to use the JdkWeb.
        bind(Web.class).to(JdkWeb.class);
      }
    }).getInstance(Web.class);

    WebClient<String> webClient = web.clientOf("http://twitter.com/statuses/public_timeline.json").transports(String.class).over(Text.class);
    final WebResponse response = webClient.get();

    assert response.toString().contains("statuses");
  }
}
