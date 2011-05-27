package com.google.sitebricks;

import com.google.inject.Guice;
import com.google.inject.Scopes;
import com.google.inject.servlet.RequestScoped;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.routing.Action;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class EdslTest {

  @Test
  public final void edsl() {

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

        // Register a custom SPI action.
        at("/stuff")
            .perform(new DummyAction())
            .on(Get.class, Post.class);

        at("/stuff/:thing")
            .perform(new DummyAction())
            .select("metadata", "form")
            .selectHeader("Accept", "image/jpeg")
            .on(Get.class);

        // Registration of embeddable widgets (simply points to page class)
        embed(EdslTest.class).as("@Blasphemy");
        embed(EdslTest.class).as("@Hiberty");
        embed(EdslTest.class).as("@Plurality").in(RequestScoped.class);
      }
    });
  }

  private static class DummyAction implements Action {
    @Override
    public boolean shouldCall(Request request) {
      return true;
    }

    @Override
    public Object call(Object page, Map<String, String> map) {
      return Reply.saying().ok();
    }
  }
}
