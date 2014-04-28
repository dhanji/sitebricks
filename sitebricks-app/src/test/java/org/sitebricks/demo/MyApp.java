package org.sitebricks.demo;

import org.sitebricks.App;
import com.google.sitebricks.SitebricksModule;
import com.google.sitebricks.channel.ChannelModule;
import org.sitebricks.web.WebModule;

/**
 * @author dhanji (Dhanji R. Prasanna)
 */
public class MyApp {
  public static void main(final String[] args) {
    new App(args)
        .modules(new WebModule(MyApp.class.getPackage()))
        .start();
  }
}
