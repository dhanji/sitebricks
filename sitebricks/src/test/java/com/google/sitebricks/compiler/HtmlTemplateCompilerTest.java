package com.google.sitebricks.compiler;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.sitebricks.Bricks;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import com.google.sitebricks.RespondersForTesting;
import com.google.sitebricks.Template;
import com.google.sitebricks.TestRequestCreator;
import com.google.sitebricks.conversion.MvelTypeConverter;
import com.google.sitebricks.conversion.TypeConverter;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.http.Delete;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Patch;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.http.Put;
import com.google.sitebricks.rendering.EmbedAs;
import com.google.sitebricks.rendering.control.Chains;
import com.google.sitebricks.rendering.control.WidgetRegistry;
import com.google.sitebricks.routing.PageBook;
import com.google.sitebricks.routing.SystemMetrics;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class HtmlTemplateCompilerTest {
  private static final String ANNOTATION_EXPRESSIONS = "Annotation expressions";
  private Injector injector;
  private WidgetRegistry registry;
  private PageBook pageBook;
  private SystemMetrics metrics;
  private final Map<String, Class<? extends Annotation>> methods = Maps.newHashMap();

  private HtmlTemplateCompiler compiler() {
    registry = injector.getInstance(WidgetRegistry.class);    
    registry.addEmbed("myfave");
    pageBook = injector.getInstance(PageBook.class);    
    pageBook.at("/somewhere", MyEmbeddedPage.class).apply(Chains.terminal());
    return new HtmlTemplateCompiler(registry, pageBook, metrics);
  }
   
  @BeforeMethod
  public void pre() {
    methods.put("get", Get.class);
    methods.put("post", Post.class);
    methods.put("put", Put.class);
    methods.put("patch", Patch.class);
    methods.put("delete", Delete.class);

    injector = Guice.createInjector(new AbstractModule() {
      protected void configure() {
        bind(new TypeLiteral<Request>(){}).toProvider(mockRequestProviderForContext());
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

    Renderable widget = compiler()
        .compile(Object.class, new Template("<html>@ShowIf(true)<p>hello</p></html>"));

//        .compile("<!doctype html>\n" +
//              "<html><head><meta charset=\"UTF-8\"><title>small test</title></head><body>\n" +
//              "@ShowIf(true)<p>hello</p>" +
//              "\n</body></html>");


    assert null != widget : " null ";

    final Respond mockRespond = RespondersForTesting.newRespond();
//        final Respond mockRespond = new StringBuilderRespond() {
//            @Override
//            public void write(String text) {
//                builder.append(text);
//            }
//
//            @Override
//            public void write(char text) {
//                builder.append(text);
//            }
//
//            @Override
//            public void chew() {
//                builder.deleteCharAt(builder.length() - 1);
//            }
//        };

    widget.render(new Object(), mockRespond);

    final String value = mockRespond.toString();
    System.out.println(value);
    assert "<html><p>hello</p></html>".equals(value) : "Did not write expected output, instead: " + value;
    // assert "<!doctype html><html><head><meta charset=\"UTF-8\"><title>small test</title></head><body><p>hello</p></body></html>".equals(value) : "Did not write expected output, instead: " + value;
  }


  @DataProvider(name = ANNOTATION_EXPRESSIONS)
  public Object[][] get() {
    return new Object[][]{
        {"true"},
        {"java.lang.Boolean.TRUE"},
        {"java.lang.Boolean.valueOf('true')"},
//        {"true ? true : true"},   @TODO (BD): Disabled until I actually investigate if this is a valid test.
        {"'x' == 'x'"},
        {"\"x\" == \"x\""},
        {"'hello' instanceof java.io.Serializable"},
        {"true; return true"},
        {" 5 >= 2 "},
    };
  }

  @Test(dataProvider = ANNOTATION_EXPRESSIONS)
  public final void readAWidgetWithVariousExpressions(String expression) {    
    Renderable widget = compiler()
        .compile(Object.class, new Template(String.format("<html>@ShowIf(%s)<p>hello</p></html>", expression)));
    
    assert null != widget : " null ";    
    final Respond mockRespond = RespondersForTesting.newRespond();    
    widget.render(new Object(), mockRespond);    
    final String value = mockRespond.toString();    
    assert "<html><p>hello</p></html>".equals(value) : "Did not write expected output, instead: " + value;
  }


  @Test
  public final void readShowIfWidgetFalse() {

    Renderable widget = compiler()
        .compile(Object.class, new Template("<html>@ShowIf(false)<p>hello</p></html>"));

    assert null != widget : " null ";
    final Respond mockRespond = RespondersForTesting.newRespond();
    widget.render(new Object(), mockRespond);
    final String value = mockRespond.toString();
    assert "<html></html>".equals(value) : "Did not write expected output, instead: " + value;
  }


  @Test
  public final void readTextWidgetValues() {

    // make a basic type converter without creating
    TypeConverter converter = new MvelTypeConverter();
    Parsing.setTypeConverter(converter);

    Renderable widget = compiler()
        .compile(TestBackingType.class, new Template("<html><div class='${clazz}'>hello <a href='/people/${id}'>${name}</a></div></html>"));

    assert null != widget : " null ";
    final Respond mockRespond = RespondersForTesting.newRespond();
    widget.render(new TestBackingType("Dhanji", "content", 12), mockRespond);
    final String value = mockRespond.toString();
    assert "<html><div class='content'>hello <a href='/people/12'>Dhanji</a></div></html>"
        .replaceAll("'", "\"")
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


  @Test
  public final void readAndRenderRequireWidget() {
 
    // make a basic type converter without creating  
    TypeConverter converter = new MvelTypeConverter();
    Parsing.setTypeConverter(converter);

    Renderable widget = compiler()
        //new HtmlTemplateCompiler(registry, pageBook, metrics)
            .compile(TestBackingType.class, new Template("<html> <head>" +
                "   @Require <script type='text/javascript' src='my.js'> </script>" +
                "   @Require <script type='text/javascript' src='my.js'> </script>" +
                "</head><body>" +
                "<div class='${clazz}'>hello <a href='/people/${id}'>${name}</a></div>" +
                "</body></html>"));
    
    assert null != widget : " null ";
    final Respond respond = RespondersForTesting.newRespond();
    widget.render(new TestBackingType("Dhanji", "content", 12), respond);

    final String value = respond.toString();
    String expected = "<html> <head>" +
        "      <script type='text/javascript' src='my.js'> </script>" +
        "</head><body>" +
        "<div class='content'>hello <a href='/people/12'>Dhanji</a></div></body></html>";
    expected = expected.replaceAll("'", "\"");
    assertEquals(value, expected);
  }


   @Test
   public final void readHtmlWidgetWithError() {
       try{
        Renderable widget = compiler()
                .compile(TestBackingType.class, new Template("<html>\n<div class='${clazz}'>hello</div>\n</html>${qwe}"));
        fail();
       } catch (Exception ex){
           assertEquals(ex.getClass(), TemplateCompileException.class);
           TemplateCompileException te = (TemplateCompileException) ex;
           assertEquals(te.getErrors().size(), 1);
           CompileError error = te.getErrors().get(0);
           assertEquals(error.getLine(), 2);
       }
   }

    @Test
    public final void readHtmlWidgetWithErrorAndWidget() {
        try{
            Renderable widget = compiler()
                    .compile(TestBackingType.class, new Template("<html>\n<div class='${clazz}'>hello</div>\n\n</html>@ShowIf(true)\n${qwe}"));
            fail();
        } catch (Exception ex){
            assertEquals(ex.getClass(), TemplateCompileException.class);
            TemplateCompileException te = (TemplateCompileException) ex;
            assertEquals(te.getErrors().size(), 1);
            CompileError error = te.getErrors().get(0);
            assertEquals(error.getLine(), 4);
        }
    }

  @Test
  public final void readHtmlWidget() {

    Renderable widget = compiler()
        .compile(TestBackingType.class, new Template("<html><div class='${clazz}'>hello</div></html>"));

    assert null != widget : " null ";
    final Respond mockRespond = RespondersForTesting.newRespond();
    widget.render(new TestBackingType("Dhanji", "content", 12), mockRespond);
    final String s = mockRespond.toString();
    assert "<html><div class=\"content\">hello</div></html>"
        .equals(s) : "Did not write expected output, instead: " + s;
  }


  @Test
  public final void readHtmlWidgetWithChildren() {

    Renderable widget = compiler()
        .compile(TestBackingType.class, new Template("<!doctype html><html><body><div class='${clazz}'>hello @ShowIf(false)<a href='/hi/${id}'>hideme</a></div></body></html>"));

    assert null != widget : " null ";
    final Respond mockRespond = RespondersForTesting.newRespond();
    widget.render(new TestBackingType("Dhanji", "content", 12), mockRespond);
    final String s = mockRespond.toString();
    assertEquals(s, "<!doctype html><html><body><div class=\"content\">hello </div></body></html>");
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

  @Test
  public final void readEmbedWidgetAndStoreAsPage() {

    Renderable widget = compiler()
        .compile(TestBackingType.class, new Template("<xml><div class='content'>hello @MyFave(should=false)<a href='/hi/${id}'>hideme</a></div></xml>"));

    assert null != widget : " null ";

    //tell pagebook to track this as an embedded widget
    pageBook.embedAs(MyEmbeddedPage.class, MyEmbeddedPage.MY_FAVE_ANNOTATION)
        .apply(Chains.terminal());

    final Respond mockRespond = RespondersForTesting.newRespond();
    widget.render(new TestBackingType("Dhanji", "content", 12), mockRespond);
    final String s = mockRespond.toString();
    assert "<xml><div class=\"content\">hello </div></xml>"
        .equals(s) : "Did not write expected output, instead: " + s;
  }


  @Test
  public final void readEmbedWidgetOnly() {

    Renderable widget = compiler()
        .compile(TestBackingType.class, new Template("<html><div class='content'>hello @MyFave(should=false)<a href='/hi/${id}'>hideme</a></div></html>"));

    assert null != widget : " null ";

    //tell pagebook to track this as an embedded widget
    pageBook.embedAs(MyEmbeddedPage.class, MyEmbeddedPage.MY_FAVE_ANNOTATION)
        .apply(Chains.terminal());

    final Respond mockRespond = RespondersForTesting.newRespond();

    widget.render(new TestBackingType("Dhanji", "content", 12), mockRespond);

    final String s = mockRespond.toString();
    assert "<html><div class=\"content\">hello </div></html>"
        .equals(s) : "Did not write expected output, instead: " + s;
  }


  //TODO Fix this test!
//    @Test
//    public final void readEmbedWidgetWithArgs() throws ExpressionCompileException {
//
//        final Evaluator evaluator = new MvelEvaluator();
//        final Injector injector = Guice.createInjector(new AbstractModule() {
//            protected void configure() {
//                bind(HttpServletRequest.class).toProvider(mockRequestProviderForContext());
//            }
//        });
//        final PageBook book = injector.getInstance(PageBook.class);           //hacky, where are you super-packages!
//
//        final WidgetRegistry registry = injector.getInstance(WidgetRegistry.class);
//
//        final MvelEvaluatorCompiler compiler = new MvelEvaluatorCompiler(TestBackingType.class);
//        Renderable widget =
//                new HtmlTemplateCompiler(Object.class, compiler, registry, book, metrics)
//                    .compile("<xml><div class='content'>hello @MyFave(should=true)<a href='/hi/${id}'> @With(\"me\")<p>showme</p></a></div></xml>");
//
//        assert null != widget : " null ";
//
//
//        HtmlWidget bodyWrapper = new XmlWidget(Chains.proceeding().addWidget(new IncludeWidget(new TerminalWidgetChain(), "'me'", evaluator)),
//                "body", compiler, Collections.<String, String>emptyMap());
//
//        bodyWrapper.setRequestProvider(mockRequestProviderForContext());
//
//        //should include the @With("me") annotated widget from the template above (discarding the <p> tag).
//        book.embedAs(MyEmbeddedPage.class).apply(bodyWrapper);
//
//        final Respond mockRespond = new StringBuilderRespond();
//
//        widget.render(new TestBackingType("Dhanji", "content", 12), mockRespond);
//
//        final String s = mockRespond.toString();
//        assert "<xml><div class=\"content\">hello showme</div></xml>"
//                .equals(s) : "Did not write expected output, instead: " + s;
//    }

  public static Provider<Request> mockRequestProviderForContext() {
    return new Provider<Request>() {
      public Request get() {
        final HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getContextPath())
            .andReturn("")
            .anyTimes();
        expect(request.getMethod())
            .andReturn("POST")
            .anyTimes();
        expect(request.getParameterMap())
            .andReturn(ImmutableMap.of())
            .anyTimes();
        replay(request);

        return TestRequestCreator.from(request, null);
      }
    };
  }

}
