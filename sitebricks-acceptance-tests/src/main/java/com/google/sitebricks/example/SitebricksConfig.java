package com.google.sitebricks.example;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.bval.guice.ValidationModule;

import com.google.common.collect.ImmutableMap;
import com.google.inject.BindingAnnotation;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.sitebricks.AwareModule;
import com.google.sitebricks.SitebricksModule;
import com.google.sitebricks.binding.FlashCache;
import com.google.sitebricks.binding.HttpSessionFlashCache;
import com.google.sitebricks.channel.ChannelListener;
import com.google.sitebricks.channel.ChannelModule;
import com.google.sitebricks.conversion.DateConverters;
import com.google.sitebricks.debug.DebugPage;
import com.google.sitebricks.example.dao.SimpleDao;
import com.google.sitebricks.example.dao.ValidatingDao;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.http.Delete;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Patch;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.http.Put;
import com.google.sitebricks.rendering.Decorated;
import com.google.sitebricks.routing.Action;
import com.google.sitebricks.stat.StatModule;

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
        install(new ChannelModule("/channel") {
          @Override
          protected void configureChannels() {
            processAll().with(Chatter.class);
            bind(ChannelListener.class).to(Chatter.ChatterListener.class);
          }
        });

        // TODO(dhanji): find a way to run the suite again with this module installed.
//        install(new GaeModule());

        bind(FlashCache.class).to(HttpSessionFlashCache.class).in(Singleton.class);

        // TODO We should run the test suite once with this turned off and with scan() on.
//        scan(SitebricksConfig.class.getPackage());
        bindExplicitly();
        bindActions();
        bindCrudActions();

        // Bind a dummy interceptor to specifically test AOP interaction with decorated pages.
        bindInterceptor(Matchers.annotatedWith(Decorated.class), Matchers.any(), new MethodInterceptor() {
          @Override
          public Object invoke(MethodInvocation methodInvocation) throws Throwable {
            return methodInvocation.proceed();
          }
        });

        at("/no_annotations/service").serve(RestfulWebServiceNoAnnotations.class);
        at("/debug").show(DebugPage.class);

        bind(Start.class).annotatedWith(Test.class).to(Start.class);

        // Localize using the default translation set (i.e. from the @Message annotations)
        localize(I18n.MyMessages.class).usingDefault();
        localize(I18n.MyMessages.class).using(Locale.CANADA_FRENCH,
            ImmutableMap.of(I18n.HELLO, I18n.HELLO_IN_FRENCH));

        localize(HtmlValidating.ErrorMessages.class).usingDefault();
        localize(HtmlValidatingAsForm.ErrorMessages.class).usingDefault();

        install(new StatModule("/stats"));

        converter(new DateConverters.DateStringConverter(DEFAULT_DATE_TIME_FORMAT));

        install(new AwareModule() {
          @Override
          protected void configureLifecycle() {
            observe(StartAware.class).asEagerSingleton();
          }
        });
        
        // Validation
        install(new ValidationModule());
      
      }

      private void bindExplicitly() {
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

        at("/helloservice").serve(HelloWorldService.class);

        at("/service").serve(RestfulWebService.class);
        at("/mimes_service").serve(RestfulWebServiceWithMimes.class);
        at("/postable").serve(PostableRestfulWebService.class);
        at("/superpath").serve(RestfulWebServiceWithSubpaths.class);
        at("/matrixpath").serve(RestfulWebServiceWithMatrixParams.class);
        at("/superpath2/:dynamic").serve(RestfulWebServiceWithSubpaths2.class);
        at("/json/:type").serve(RestfulWebServiceWithCRUD.class);
        at("/jsonConversion").serve(RestfulWebServiceWithCRUDConversions.class);
        at("/serviceWithGenerics").serve(RestfulWebServiceWithGenerics.class);

        at("/pagechain").show(PageChain.class);
        at("/nextpage").show(NextPage.class);

        at("/i18n").show(I18n.class);

        // MVEL template.
        at("/template/mvel").show(MvelTemplateExample.class);

        // templating by extension
        at("/template").show(DecoratedPage.class);

        at("/chat").show(Chatter.class);

        at("/decorated-repeat").show(DecoratedRepeat.class);

        at("/jsp").show(Jsp.class);

        embed(HelloWorld.class).as("Hello");
        
        // Validation
        bind(SimpleDao.class).to(ValidatingDao.class);
        at("/htmlvalidating").show(HtmlValidating.class);
        at("/htmlvalidatingasform").show(HtmlValidatingAsForm.class);
        at("/jspvalidating").show(JspValidating.class);
        at("/jspvalidatingasform").show(JspValidatingAsForm.class);
        at("/restvalidating").serve(RestfulWebServiceValidating.class);
        at("/restvalidatingdao").serve(RestfulWebServiceValidatingDao.class);
        
      }

      @SuppressWarnings("unchecked")
      private void bindActions() {
        at("/spi/test")
            .perform(action("get:top"))
            .on(Get.class)
            .perform(action("post:junk_subpath1"))
            .on(Post.class);
      }

      @SuppressWarnings("unchecked")
      private void bindCrudActions() {
        //
        // Handle the base path
        //
        at("/issue")
            .perform(action("READ_COLLECTION"))
            .on(Get.class)
            .perform(action("CREATE"))
            .on(Post.class);

        //
        // Handle subpaths for verbs that have parameters
        //
        at("/issue/:id")
            .perform(action("READ"))
            .on(Get.class)
            .perform(action("UPDATE"))
            .on(Put.class)
            .perform(action("PARTIAL_UPDATE"))
            .on(Patch.class)
            .perform(action("DELETE"))
            .on(Delete.class);
      }
    });
  }

  private Action action(final String reply) {
    return new Action() {
      @Override
      public boolean shouldCall(Request request) {
        return true;
      }

      @Override
      public Object call(Request request, Object page, Map<String, String> map) {
        return Reply.with(reply);
      }
    };
  }

  @BindingAnnotation
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Test {
  }
}
