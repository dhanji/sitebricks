package com.google.sitebricks;

import com.google.inject.Guice;
import com.google.inject.Scopes;
import com.google.inject.servlet.RequestScoped;

import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class EdslTest {

  @Test
  public final void edsl() {

    assert new Integer(11) == (new Byte((byte)11).intValue());
    assert new Byte((byte)11).equals(new Integer(11)):"kk" ;
    Guice.createInjector(new SitebricksModule() {

      @Override
      protected void configureSitebricks() {

        // Registration of page classes
        at("/rpc")
            .show(EdslTest.class)
            .asEagerSingleton();

        at("/pub")
            .show(EdslTest.class)
            .in(Scopes.NO_SCOPE);

        // Registration of static resources (bundled in jar)
        at("/script.js").export("/client/my.js");

        // Registration of embeddable widgets (simply points to page class)
        embed(EdslTest.class).as("@Blasphemy");
        embed(EdslTest.class).as("@Hiberty");
        embed(EdslTest.class).as("@Plurality").in(RequestScoped.class);
      }
    });
  }
}
