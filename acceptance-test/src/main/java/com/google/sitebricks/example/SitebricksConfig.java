package com.google.sitebricks.example;

import com.google.common.collect.ImmutableMap;
import com.google.inject.BindingAnnotation;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.sitebricks.SitebricksModule;
import com.google.sitebricks.binding.FlashCache;
import com.google.sitebricks.binding.HttpSessionFlashCache;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

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
        // TODO(dhanji): find a way to run the suite again with this module installed.
//        install(new GaeModule());

        bind(FlashCache.class).to(HttpSessionFlashCache.class).in(Singleton.class);

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

        at("/hiddenfieldmethod").show(HiddenFieldMethod.class);
        at("/select").show(SelectRouting.class);
        at("/conneg").show(ContentNegotiation.class);

        at("/service").serve(RestfulWebService.class);
        at("/no_annotations/service").serve(RestfulWebServiceNoAnnotations.class);

        at("/pagechain").show(PageChain.class);
        at("/nextpage").show(NextPage.class);

        at("/i18n").show(I18n.class);

        // MVEL template.
        at("/template/mvel").show(MvelTemplateExample.class);

        bind(Start.class).annotatedWith(Test.class).to(Start.class);

        embed(HelloWorld.class).as("Hello");

        // Localize using the default translation set (i.e. from the @Message annotations)
        localize(I18n.MyMessages.class).usingDefault();
        localize(I18n.MyMessages.class).using(Locale.CANADA_FRENCH,
            ImmutableMap.of(I18n.HELLO, I18n.HELLO_IN_FRENCH));
      }
    });
  }

  @BindingAnnotation
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Test {
  }
}
