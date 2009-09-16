package com.google.sitebricks.cards;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.sitebricks.SitebricksModule;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class Cards extends GuiceServletContextListener {

  @Override
  protected Injector getInjector() {
    return Guice.createInjector(new SitebricksModule() {

      @Override
      protected void configureSitebricks() {


        // Make NewCard an embeddable brick.
        embed(NewCard.class).as("Card");
      }

    });
  }
}
