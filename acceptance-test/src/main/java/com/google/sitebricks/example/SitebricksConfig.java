package com.google.sitebricks.example;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

import com.google.common.collect.ImmutableMap;
import com.google.inject.BindingAnnotation;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.stat.StatModule;
import com.google.sitebricks.SitebricksModule;
import com.google.sitebricks.binding.FlashCache;
import com.google.sitebricks.binding.HttpSessionFlashCache;
import com.google.sitebricks.conversion.DateConverters;
import com.google.sitebricks.debug.DebugPage;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class SitebricksConfig extends GuiceServletContextListener {

  // a weird format
  public static final String DEFAULT_DATE_TIME_FORMAT = "dd MM yy SS";

@Override
  protected Injector getInjector() {
    return Guice.createInjector(Stage.DEVELOPMENT, new SitebricksModule() {

      @Override
      protected void configureSitebricks() {

        // TODO(dhanji): find a way to run the suite again with this module installed.
//        install(new GaeModule());

        bind(FlashCache.class).to(HttpSessionFlashCache.class).in(Singleton.class);

        // TODO We should run the test suite once with this turned off and with scan() on.
//        scan(SitebricksConfig.class.getPackage());
        bindExplicitly();

        at("/no_annotations/service").serve(RestfulWebServiceNoAnnotations.class);
        at("/debug").show(DebugPage.class);

        bind(Start.class).annotatedWith(Test.class).to(Start.class);

        // Localize using the default translation set (i.e. from the @Message annotations)
        localize(I18n.MyMessages.class).usingDefault();
        localize(I18n.MyMessages.class).using(Locale.CANADA_FRENCH,
            ImmutableMap.of(I18n.HELLO, I18n.HELLO_IN_FRENCH));
        
        install(new StatModule("/stats"));
        
        converter(new DateConverters.DateStringConverter(DEFAULT_DATE_TIME_FORMAT));
      }

      private void bindExplicitly() {
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
        
        at("/conversion").show(Conversion.class);

        at("/hiddenfieldmethod").show(HiddenFieldMethod.class);
        at("/select").show(SelectRouting.class);
        at("/conneg").show(ContentNegotiation.class);

        at("/service").serve(RestfulWebService.class);
        at("/postable").serve(PostableRestfulWebService.class);
        at("/superpath").serve(RestfulWebServiceWithSubpaths.class);
        at("/superpath2/:dynamic").serve(RestfulWebServiceWithSubpaths2.class);
        at("/json/:type").serve(RestfulWebServiceWithCRUD.class);
        at("/jsonConversion").serve(RestfulWebServiceWithCRUDConversions.class);

        at("/pagechain").show(PageChain.class);
        at("/nextpage").show(NextPage.class);

        at("/i18n").show(I18n.class);

        // MVEL template.
        at("/template/mvel").show(MvelTemplateExample.class);

        embed(HelloWorld.class).as("Hello");
      }
    });
  }

  @BindingAnnotation
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Test {
  }
}
