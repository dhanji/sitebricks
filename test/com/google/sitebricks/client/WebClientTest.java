package com.google.sitebricks.client;

import com.google.inject.Guice;
import org.testng.annotations.Test;
import org.mvel.util.ParseTools;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class WebClientTest {

  @Test
  public final void simpleJsonGetFromTwitter() {
    Web web = Guice.createInjector().getInstance(Web.class);

    WebClient<String> webClient = web.clientOf("http://twitter.com/statuses/public_timeline.xml")
        .transports(String.class)
        .over(Json.class);

    final WebResponse response = webClient.get();

    System.out.println(response.toString());

  }
}