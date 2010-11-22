package com.google.sitebricks.compiler;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.testng.Assert.assertEquals;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.sitebricks.Bricks;
import com.google.sitebricks.Evaluator;
import com.google.sitebricks.MvelEvaluator;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import com.google.sitebricks.RespondersForTesting;
import com.google.sitebricks.compiler.template.freemarker.FreemarkerTemplateCompiler;
import com.google.sitebricks.http.Delete;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.http.Put;
import com.google.sitebricks.rendering.EmbedAs;
import com.google.sitebricks.rendering.control.Chains;
import com.google.sitebricks.rendering.control.WidgetRegistry;
import com.google.sitebricks.routing.PageBook;
import com.google.sitebricks.routing.SystemMetrics;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class FreemarkerTemplateCompilerTest {
  private static final String ANNOTATION_EXPRESSIONS = "Annotation expressions";
  private Injector injector;
  private PageBook pageBook;
  private SystemMetrics metrics;
  private final Map<String, Class<? extends Annotation>> methods = Maps.newHashMap();

  @BeforeMethod
  public void pre() {
    methods.put("get", Get.class);
    methods.put("post", Post.class);
    methods.put("put", Put.class);
    methods.put("delete", Delete.class);

    injector = Guice.createInjector(new AbstractModule() {
      protected void configure() {
        bind(HttpServletRequest.class).toProvider(mockRequestProviderForContext());
        bind(new TypeLiteral<Map<String, Class<? extends Annotation>>>() {
        })
            .annotatedWith(Bricks.class)
            .toInstance(methods);
      }
    });

    pageBook = createNiceMock(PageBook.class);
    metrics = createNiceMock(SystemMetrics.class);
  }

  @Test
  public final void annotationKeyExtraction() {
    assert "link".equals(Dom.extractKeyAndContent("@Link")[0]) : "Extraction wrong: ";
    assert "thing".equals(Dom.extractKeyAndContent("@Thing()")[0]) : "Extraction wrong: ";
    assert "thing".equals(Dom.extractKeyAndContent("@Thing(asodkoas)")[0]) : "Extraction wrong: ";
    assert "thing".equals(Dom.extractKeyAndContent("@Thing(asodkoas)  ")[0]) : "Extraction wrong: ";
    assert "thing".equals(Dom.extractKeyAndContent("@Thing(asodkoas)  kko")[0]) : "Extraction wrong: ";

    assert "".equals(Dom.extractKeyAndContent("@Link")[1]) : "Extraction wrong: ";
    final String val = Dom.extractKeyAndContent("@Thing()")[1];
    assert null == (val) : "Extraction wrong: " + val;
    assert "asodkoas".equals(Dom.extractKeyAndContent("@Thing(asodkoas)")[1]) : "Extraction wrong: ";
    assert "asodkoas".equals(Dom.extractKeyAndContent("@Thing(asodkoas)  ")[1]) : "Extraction wrong: ";
    assert "asodkoas".equals(Dom.extractKeyAndContent("@Thing(asodkoas)  kko")[1]) : "Extraction wrong: ";
  }

  @Test
  public final void readShowIfWidgetTrue() {

    Renderable widget =
        new FreemarkerTemplateCompiler(Object.class)
          .compile("<html><#if true><p>hello</p></#if></html>");

    assert null != widget : " null ";

    final StringBuilder builder = new StringBuilder();
    final Respond mockRespond = RespondersForTesting.newRespond();
    widget.render(new Object(), mockRespond);
    final String value = mockRespond.toString();
    System.out.println(value);
    assert "<html><p>hello</p></html>".equals(value) : "Did not write expected output, instead: " + value;
  }


  @DataProvider(name = ANNOTATION_EXPRESSIONS)
  public Object[][] get() {
    return new Object[][]{
        {"true"},
//        {"java.lang.Boolean.TRUE"},
//        {"java.lang.Boolean.valueOf('true')"},
//        {"true ? true : true"},   @TODO (BD): Disabled until I actually investigate if this is a valid test.
        {"'x' == 'x'"},
        {"\"x\" == \"x\""},
//        {"'hello' instanceof java.io.Serializable"},
//        {"true; return true"},
//        {" 5 >= 2 "},
    };
  }

  @Test(dataProvider = ANNOTATION_EXPRESSIONS)
  public final void readAWidgetWithVariousExpressions(String expression) {
    final Evaluator evaluator = new MvelEvaluator();

    final WidgetRegistry registry = injector.getInstance(WidgetRegistry.class);

    String templateValue = String.format("<html><#if %s><p>hello</p></#if></html>", expression);

    System.out.println( templateValue );
    
    Renderable widget =
        new FreemarkerTemplateCompiler(Object.class)
            .compile(templateValue);

    assert null != widget : " null ";

    final StringBuilder builder = new StringBuilder();

    final Respond mockRespond = RespondersForTesting.newRespond();

    widget.render(new Object(), mockRespond);

    final String value = mockRespond.toString();
    System.out.println(value);
    assert "<html><p>hello</p></html>".equals(value) : "Did not write expected output, instead: " + value;
  }


  @Test
  public final void readShowIfWidgetFalse() {
    final Injector injector = Guice.createInjector(new AbstractModule() {
      protected void configure() {
        bind(HttpServletRequest.class).toProvider(mockRequestProviderForContext());
      }
    });

    final Evaluator evaluator = new MvelEvaluator();

    final WidgetRegistry registry = injector.getInstance(WidgetRegistry.class);


    Renderable widget =
        new FreemarkerTemplateCompiler(Object.class)
            .compile("<html><#if false><p>hello</p></#if></html>");

    assert null != widget : " null ";

    final StringBuilder builder = new StringBuilder();

    final Respond mockRespond = RespondersForTesting.newRespond();
    widget.render(new Object(), mockRespond);

    final String value = mockRespond.toString();
    assert "<html></html>".equals(value) : "Did not write expected output, instead: " + value;
  }


  @Test
  public final void readTextWidgetValues() {
    final Injector injector = Guice.createInjector(new AbstractModule() {
      protected void configure() {
        bind(HttpServletRequest.class).toProvider(mockRequestProviderForContext());
      }
    });

    Renderable widget =
        new FreemarkerTemplateCompiler(Object.class)
            .compile("<html><div class='${clazz}'>hello <a href='/people/${id}'>${name}</a></div></html>");

    assert null != widget : " null ";


    final Respond mockRespond = RespondersForTesting.newRespond();

    widget.render(new TestBackingType("Dhanji", "content", 12), mockRespond);

    final String value = mockRespond.toString();
    assert "<html><div class='content'>hello <a href='/people/12'>Dhanji</a></div></html>"
        .replace("\"", "'")
        .equals(value) : "Did not write expected output, instead: " + value;
  }

  public static class TestBackingType {
    private String name;
    private String clazz;
    private Integer id;

    public TestBackingType(String name, String clazz, Integer id) {
      this.name = name;
      this.clazz = clazz;
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public String getClazz() {
      return clazz;
    }

    public Integer getId() {
      return id;
    }
  }


//  @Test
//  public final void readAndRenderRequireWidget() {
//    final Injector injector = Guice.createInjector(new AbstractModule() {
//      protected void configure() {
//        bind(HttpServletRequest.class).toProvider(mockRequestProviderForContext());
//        bind(new TypeLiteral<Map<String, Class<? extends Annotation>>>() {
//        })
//            .annotatedWith(Bricks.class)
//            .toInstance(methods);
//      }
//    });
//
//
//    final PageBook pageBook = injector.getInstance(PageBook.class);
//
//
//    final WidgetRegistry registry = injector.getInstance(WidgetRegistry.class);
//
//
//    Renderable widget =
//        new FreemarkerTemplateCompiler(Object.class)
//            .compile("<html> <head>" +
//                "   @Require <script type='text/javascript' src='my.js'> </script>" +
//                "   @Require <script type='text/javascript' src='my.js'> </script>" +
//                "</head><body>" +
//                "<div class='${clazz}'>hello <a href='/people/${id}'>${name}</a></div>" +
//                "</body></html>");
//
//    assert null != widget : " null ";
//
//    final Respond respond = RespondersForTesting.newRespond();
//
//    widget.render(new TestBackingType("Dhanji", "content", 12), respond);
//
//    final String value = respond.toString();
//    String expected = "<html> <head>" +
//        "      <script type='text/javascript' src='my.js'></script>" +
//        "</head><body>" +
//        "<div class='content'>hello <a href='/people/12'>Dhanji</a></div></body></html>";
//    expected = expected.replaceAll("'", "\"");
//
//    assertEquals(value, expected);
//  }


  @Test
  public final void readHtmlWidget() {

    final WidgetRegistry registry = injector.getInstance(WidgetRegistry.class);

    Renderable widget =
        new FreemarkerTemplateCompiler(Object.class)
            .compile("<html><div class='${clazz}'>hello</div></html>");

    assert null != widget : " null ";


    final Respond mockRespond = RespondersForTesting.newRespond();

    widget.render(new TestBackingType("Dhanji", "content", 12), mockRespond);

    final String s = mockRespond.toString();
    assert "<html><div class=\"content\">hello</div></html>"
        .replace( "\"", "'")                
        .equals(s) : "Did not write expected output, instead: " + s;
  }


  @Test
  public final void readHtmlWidgetWithChildren() {

    final WidgetRegistry registry = injector.getInstance(WidgetRegistry.class);

    Renderable widget =
        new FreemarkerTemplateCompiler(Object.class)
            .compile("<!doctype html><html><body><div class='${clazz}'>hello <#if false><a href='/hi/${id}'>hideme</a></#if></div></body></html>");

    assert null != widget : " null ";


    final Respond mockRespond = RespondersForTesting.newRespond();

    widget.render(new TestBackingType("Dhanji", "content", 12), mockRespond);

    final String s = mockRespond.toString();
    assertEquals(s, "<!doctype html><html><body><div class=\"content\">hello </div></body></html>".replace("\"", "'"));
  }

  @EmbedAs(MyEmbeddedPage.MY_FAVE_ANNOTATION)
  public static class MyEmbeddedPage {
    protected static final String MY_FAVE_ANNOTATION = "MyFave";
    private boolean should = true;

    public boolean isShould() {
      return should;
    }

    public void setShould(boolean should) {
      this.should = should;
    }
  }

//  @Test
//  public final void readEmbedWidgetAndStoreAsPage() {
//    final Injector injector = Guice.createInjector(new AbstractModule() {
//      protected void configure() {
//        bind(HttpServletRequest.class).toProvider(mockRequestProviderForContext());
//        bind(new TypeLiteral<Map<String, Class<? extends Annotation>>>() {
//        })
//            .annotatedWith(Bricks.class)
//            .toInstance(methods);
//      }
//    });
//    final PageBook book = injector      //hacky, where are you super-packages!
//        .getInstance(PageBook.class);
//
//    book.at("/somewhere", MyEmbeddedPage.class).apply(Chains.terminal());
//
//
//    final WidgetRegistry registry = injector.getInstance(WidgetRegistry.class);
//    registry.addEmbed("myfave");
//
//    Renderable widget =
//        new FreemarkerTemplateCompiler(Object.class)
//            .compile("<xml><div class='content'>hello @MyFave(should=false)<a href='/hi/${id}'>hideme</a></div></xml>");
//
//    assert null != widget : " null ";
//
//    //tell pagebook to track this as an embedded widget
//    book.embedAs(MyEmbeddedPage.class, MyEmbeddedPage.MY_FAVE_ANNOTATION)
//        .apply(Chains.terminal());
//
//    final Respond mockRespond = RespondersForTesting.newRespond();
//
//    widget.render(new TestBackingType("Dhanji", "content", 12), mockRespond);
//
//    final String s = mockRespond.toString();
//    assert "<xml><div class=\"content\">hello </div></xml>"
//        .equals(s) : "Did not write expected output, instead: " + s;
//  }


//  @Test
//  public final void readEmbedWidgetOnly() {
//    final Injector injector = Guice.createInjector(new AbstractModule() {
//      protected void configure() {
//        bind(HttpServletRequest.class).toProvider(mockRequestProviderForContext());
//        bind(new TypeLiteral<Map<String, Class<? extends Annotation>>>() {
//        })
//            .annotatedWith(Bricks.class)
//            .toInstance(methods);
//      }
//    });
//    final PageBook book = injector      //hacky, where are you super-packages!
//        .getInstance(PageBook.class);
//
//
//    final WidgetRegistry registry = injector.getInstance(WidgetRegistry.class);
//    registry.addEmbed("myfave");
//
//    Renderable widget =
//        new FreemarkerTemplateCompiler(Object.class)
//            .compile("<html><div class='content'>hello @MyFave(should=false)<a href='/hi/${id}'>hideme</a></div></html>");
//
//    assert null != widget : " null ";
//
//    //tell pagebook to track this as an embedded widget
//    book.embedAs(MyEmbeddedPage.class, MyEmbeddedPage.MY_FAVE_ANNOTATION)
//        .apply(Chains.terminal());
//
//    final Respond mockRespond = RespondersForTesting.newRespond();
//
//    widget.render(new TestBackingType("Dhanji", "content", 12), mockRespond);
//
//    final String s = mockRespond.toString();
//    assert "<html><div class=\"content\">hello </div></html>"
//        .replace( "\"", "'" )
//        .equals(s) : "Did not write expected output, instead: " + s;
//  }

  static Provider<HttpServletRequest> mockRequestProviderForContext() {
    return new Provider<HttpServletRequest>() {
      public HttpServletRequest get() {
        final HttpServletRequest request = createMock(HttpServletRequest.class);
        expect(request.getContextPath())
            .andReturn("")
            .anyTimes();
        replay(request);

        return request;
      }
    };
  }

}