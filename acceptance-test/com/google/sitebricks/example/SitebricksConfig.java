package com.google.sitebricks.example;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.sitebricks.SitebricksModule;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class SitebricksConfig extends GuiceServletContextListener {

  @Override
  protected Injector getInjector() {
    return Guice.createInjector(Stage.DEVELOPMENT, new SitebricksModule() {

      @Override
      protected void configureSitebricks() {
//        scan(SitebricksConfig.class.getPackage());

        //TODO explicit bindings should override scanned ones.
        at("/").show(Start.class);
        at("/hello").show(HelloWorld.class);
        at("/case").show(Case.class);
        at("/embed").show(Embed.class);
        at("/error").show(CompileErrors.class);
        at("/forms").show(Forms.class);
        at("/repeat").show(Repeat.class);
        at("/showif").show(ShowIf.class);
        at("/dynamic.js").show(DynamicJs.class);

        embed(HelloWorld.class).as("Hello");
      }

    });
  }
}
