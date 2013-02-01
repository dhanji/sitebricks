package com.google.sitebricks.routing;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.Iterators;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.google.sitebricks.At;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import com.google.sitebricks.SitebricksModule;
import com.google.sitebricks.TestRequestCreator;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.http.Select;
import com.google.sitebricks.rendering.EmbedAs;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class PageBookImplTest {
  private static final String FIRST_PATH_ELEMENTS = "firstPathElements";
  private static final String URI_TEMPLATES_AND_MATCHES = "uriTemplatesAndMatches";
  private static final String NOT_URIS_AND_TEMPLATES = "noturisandTemplates";
  private static final String REDIRECTED_GET = "/redirected_get";
  private static final String REDIRECTED_POST = "/redirected_post";

  private Injector injector;

  @BeforeTest
  public final void pre() {
    injector = Guice.createInjector(new SitebricksModule());
  }

  @Test
  public final void storeAndRetrievePageInstance() {
    final Respond respond = new MockRespond();

    Renderable mock = new Renderable() {
      public void render(Object bound, Respond respond) {

      }

      public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return null;
      }
    };

    final PageBook pageBook = new DefaultPageBook(injector);
    pageBook.at("/wiki", MyPage.class);

    PageBook.Page page = pageBook.get("/wiki");
    page.apply(mock);
    page.widget().render(new Object(), respond);

    assert page.widget().equals(mock);
  }

  @Test
  public final void fireGetMethodOnPage() throws IOException {
    Renderable mock = new Renderable() {
      public void render(Object bound, Respond respond) {

      }

      public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return null;
      }
    };

    final PageBook pageBook = new DefaultPageBook(injector);
    pageBook.at("/wiki", MyPage.class);

    PageBook.Page page = pageBook.get("/wiki");
    page.apply(mock);
    final MyPage bound = new MyPage();
    page.doMethod("get", bound, "/wiki", fakeRequestWithParams(new HashMap<String, String[]>()));

    assert page.widget().equals(mock);
    assert bound.getted : "@Get method was not fired, on doGet()";
  }

  @Test
  public final void fireGetMethodOnPageAndRedirectToURL() throws IOException {
    Renderable mock = new Renderable() {
      public void render(Object bound, Respond respond) {

      }

      public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return null;
      }
    };

    final PageBook pageBook = new DefaultPageBook(injector);
    pageBook.at("/wiki", MyRedirectingPage.class);

    PageBook.Page page = pageBook.get("/wiki");
    page.apply(mock);
    final MyRedirectingPage bound = new MyRedirectingPage();
    Object redirect = page.doMethod("get", bound, "/wiki", fakeRequestWithParams(new HashMap<String, String[]>()));

    assert REDIRECTED_GET.equals(redirect);
    assert page.widget().equals(mock);
  }

  @Test
  public final void firePostMethodOnPageAndRedirectToURL() throws IOException {
    Renderable mock = new
        Renderable() {
          public void render(Object bound, Respond respond) {

          }

          public <T extends Renderable> Set<T> collect(Class<T> clazz) {
            return null;
          }
        };

    final PageBook pageBook = new DefaultPageBook(injector);
    pageBook.at("/wiki", MyRedirectingPage.class);

    PageBook.Page page = pageBook.get("/wiki");
    page.apply(mock);
    final MyRedirectingPage bound = new MyRedirectingPage();
    Object redirect = page.doMethod("post", bound, "/wiki", fakeRequestWithParams(new HashMap<String, String[]>()));

    assert REDIRECTED_POST.equals(redirect);
    assert page.widget().equals(mock);
  }
  
  @Test
  public final void fireGetMethodOnPageAndReply403() throws IOException {
    Renderable mock = new Renderable() {
      public void render(Object bound, Respond respond) {

      }

      public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return null;
      }
    };

    final PageBook pageBook = new DefaultPageBook(injector);
    pageBook.at("/forbidden", MyForbiddenPage.class);

    PageBook.Page page = pageBook.get("/forbidden");
    page.apply(mock);
    final MyForbiddenPage bound = new MyForbiddenPage();
    Object redirect = page.doMethod("get", bound, "/forbidden", fakeRequestWithParams(new HashMap<String, String[]>()));

    assert Reply.saying().forbidden().equals(redirect);
    assert page.widget().equals(mock);
  }

  @Test
  public final void firePostMethodOnPageAndReply403() throws IOException {
    Renderable mock = new Renderable() {
        public void render(Object bound, Respond respond) {

        }

        public <T extends Renderable> Set<T> collect(Class<T> clazz) {
          return null;
        }
      };

      final PageBook pageBook = new DefaultPageBook(injector);
      pageBook.at("/forbidden", MyForbiddenPage.class);

      PageBook.Page page = pageBook.get("/forbidden");
      page.apply(mock);
      final MyForbiddenPage bound = new MyForbiddenPage();
      Object redirect = page.doMethod("post", bound, "/forbidden", fakeRequestWithParams(new HashMap<String, String[]>()));

      assert Reply.saying().forbidden().equals(redirect);
      assert page.widget().equals(mock);
  }

  @Test
  public final void fireGetMethodOnPageToCorrectHandler() throws IOException {
    Renderable mock = new Renderable() {
      public void render(Object bound, Respond respond) {

      }

      public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return null;
      }
    };

    Map<String, String[]> params = new HashMap<String, String[]>() {
      {
        put("event", new String[]{"1", "2"});
      }
    };

    final PageBook pageBook = new DefaultPageBook(injector);
    pageBook.at("/wiki", MyEventSupportingPage.class);

    PageBook.Page page = pageBook.get("/wiki");
    page.apply(mock);
    final MyEventSupportingPage bound = new MyEventSupportingPage();
    page.doMethod("get", bound, "/wiki", fakeRequestWithParams(params));

    assert page.widget().equals(mock);
    assert bound.getted1 : "@Get @On method was not fired, on doGet() for [event=1]";
    assert bound.getted2 : "@Get @On method was not fired, on doGet() for [event=2]";
  }

  @Test
  public final void firePostMethodOnPageToCorrectHandler() throws IOException {
    Renderable mock = new Renderable() {
      public void render(Object bound, Respond respond) {

      }

      public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return null;
      }
    };

    Map<String, String[]> params = new HashMap<String, String[]>() {
      {
        put("event", new String[]{"1", "2"});
      }
    };

    final PageBook pageBook = new DefaultPageBook(injector);
    pageBook.at("/wiki", MyEventSupportingPage.class);

    PageBook.Page page = pageBook.get("/wiki");
    page.apply(mock);
    final MyEventSupportingPage bound = new MyEventSupportingPage();
    page.doMethod("post", bound, "/wiki", fakeRequestWithParams(params));

    assert page.widget().equals(mock);
    assert bound.posted1 : "@Post @On method was not fired, on doPost() for [event=1]";
    assert bound.posted2 : "@Post @On method was not fired, on doPost() for [event=2]";
  }

  @Test
  public final void fireGetMethodOnPageToCorrectHandlerOnlyOnce() throws IOException {
    Renderable mock = new Renderable() {
      public void render(Object bound, Respond respond) {

      }

      public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return null;
      }
    };

    Map<String, String[]> params = new HashMap<String, String[]>() {
      {
        put("event", new String[]{"2"});
      }
    };

    final PageBook pageBook = new DefaultPageBook(injector);
    pageBook.at("/wiki", MyEventSupportingPage.class);

    PageBook.Page page = pageBook.get("/wiki");
    page.apply(mock);
    final MyEventSupportingPage bound = new MyEventSupportingPage();
    page.doMethod("get", bound, "/wiki", fakeRequestWithParams(params));

    assert page.widget().equals(mock);
    assert !bound.getted1 : "@Get @On method was fired, on doGet() for [event=1]";
    assert bound.getted2 : "@Get @On method was not fired, on doGet() for [event=2]";
  }

  @Test
  public final void firePostMethodOnPageToCorrectHandlerOnlyOnce() throws IOException {
    Renderable mock = new Renderable() {
      public void render(Object bound, Respond respond) {

      }

      public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return null;
      }
    };

    Map<String, String[]> params = new HashMap<String, String[]>() {
      {
        put("event", new String[]{"2"});
      }
    };

    final PageBook pageBook = new DefaultPageBook(injector);
    pageBook.at("/wiki", MyEventSupportingPage.class);

    PageBook.Page page = pageBook.get("/wiki");
    page.apply(mock);
    final MyEventSupportingPage bound = new MyEventSupportingPage();
    page.doMethod("post", bound, "/wiki", fakeRequestWithParams(params));

    assert page.widget().equals(mock);
    assert !bound.posted1 : "@Post @On method was fired, on doGet() for [event=1]";
    assert bound.posted2 : "@Post @On method was not fired, on doGet() for [event=2]";
  }

  @Test
  public final void fireGetMethodOnPageToDefaultHandler() throws IOException {
    Renderable mock = new Renderable() {
      public void render(Object bound, Respond respond) {

      }

      public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return null;
      }
    };

    Map<String, String[]> params = new HashMap<String, String[]>() {
      {
        put("event", new String[]{"3"});
      }
    };

    final PageBook pageBook = new DefaultPageBook(injector);
    pageBook.at("/wiki", MyEventSupportingPage.class);

    PageBook.Page page = pageBook.get("/wiki");
    page.apply(mock);
    final MyEventSupportingPage bound = new MyEventSupportingPage();
    page.doMethod("get", bound, "/wiki", fakeRequestWithParams(params));

    assert page.widget().equals(mock);
    assert !bound.getted1 : "@Get @On method was fired, on doGet() for [event=1]";
    assert !bound.getted2 : "@Get @On method was fired, on doGet() for [event=2]";
    assert bound.defaultGet : "@Get @On default method was not fired, on doGet() for [event=...]";

  }


  @Test
  public final void firePostMethodOnPageToDefaultHandler() throws IOException {
    Renderable mock = new Renderable() {
      public void render(Object bound, Respond respond) {

      }

      public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return null;
      }
    };

    Map<String, String[]> params = new HashMap<String, String[]>() {
      {
        put("event", new String[]{"3"});
      }
    };

    final PageBook pageBook = new DefaultPageBook(injector);
    pageBook.at("/wiki", MyEventSupportingPage.class);

    PageBook.Page page = pageBook.get("/wiki");
    page.apply(mock);
    final MyEventSupportingPage bound = new MyEventSupportingPage();
    page.doMethod("post", bound, "/wiki", fakeRequestWithParams(params));

    assert page.widget().equals(mock);
    assert !bound.getted2 : "@Get @On method was fired, on doPost() for [event=2]";
    assert !bound.getted1 : "@Get @On method was fired, on doPost() for [event=1]";
    assert !bound.posted1 : "@Post @On method was fired, on doPost() for [event=1]";
    assert !bound.posted2 : "@Post @On method was fired, on doPost() for [event=2]";
    assert bound.defaultPost : "@Post @On default method was not fired, on doPost() for [event=...]";

  }

  @Test
  public final void fireGetMethodWithArgsOnPage() throws IOException {
    Renderable mock = new Renderable() {
      public void render(Object bound, Respond respond) {

      }

      public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return null;
      }
    };

    final PageBook pageBook = new DefaultPageBook(injector);
    pageBook.at("/wiki/:title", MyPageWithTemplate.class);

    PageBook.Page page = pageBook.get("/wiki/IMAX");
    page.apply(mock);
    final MyPageWithTemplate bound = new MyPageWithTemplate();
    page.doMethod("get", bound, "/wiki/IMAX", fakeRequestWithParams(new HashMap<String, String[]>()));

    assert page.widget().equals(mock);
    assert "IMAX".equals(bound.title) : "@Get method was not fired, on doGet() with the right arg, instead: " + bound.title;
  }

  
  @Test
  public final void fireGetMethodWithPrimitiveArgsOnPage() throws IOException {
    Renderable mock = new Renderable() {
      public void render(Object bound, Respond respond) {

      }

      public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return null;
      }
    };

//    SimpleDateFormat sdf = new SimpleDateFormat(StringToDateTimeCalendarConverter.USA_SHORT);
    
    final PageBook pageBook = new DefaultPageBook(injector);
//    pageBook.at("/wiki/:title/cat/:int/:bool/:float/:date", MyPageWithPrimitivesTemplate.class);
    pageBook.at("/wiki/:title/cat/:int/:bool/:float", MyPageWithPrimitivesTemplate.class);

    String targetURL = "/wiki/IMAX/cat/1/true/2.5";
    PageBook.Page page = pageBook.get(targetURL);
    page.apply(mock);
    final MyPageWithPrimitivesTemplate bound = new MyPageWithPrimitivesTemplate();
    page.doMethod("get", bound, targetURL, fakeRequestWithParams(new HashMap<String, String[]>()));

    assert page.widget().equals(mock);
    assert "IMAX".equals(bound.title) && bound.id == 1 && bound.bool == true && bound.flt == 2.5
//    	&& sdf.format(date).equals(sdf.format(bound.date)): 
    	 : "@Get method did not bind in args correctly, title: " + bound.title + 
//    	" id: " + bound.id + " bool: " + bound.bool + " float: " + bound.flt + " date: " +  sdf.format(bound.date);
    	" id: " + bound.id + " bool: " + bound.bool + " float: " + bound.flt;
    ;
  }
  

  @Test
  public final void firePostMethodWithArgsOnPage() throws IOException {
    Renderable mock = new
        Renderable() {
          public void render(Object bound, Respond respond) {

          }

          public <T extends Renderable> Set<T> collect(Class<T> clazz) {
            return null;
          }
        };

    final PageBook pageBook = new DefaultPageBook(injector);
    pageBook.at("/wiki/:title/cat/:id", MyPageWithTemplate.class);

    PageBook.Page page = pageBook.get("/wiki/IMAX_P/cat/12");
    page.apply(mock);
    final MyPageWithTemplate bound = new MyPageWithTemplate();
    page.doMethod("post", bound, "/wiki/IMAX_P/cat/12", fakeRequestWithParams(new HashMap<String, String[]>()));

    assert page.widget().equals(mock);
    assert "IMAX_P".equals(bound.post) && "12".equals(bound.id)
        : "@Post method was not fired, on doPost() with the right arg, instead: " + bound.post;
  }

  @Test(expectedExceptions = InvalidEventHandlerException.class)
  public final void errorOnPostMethodWithUnnamedArgs() throws IOException {
    Renderable mock = new Renderable() {
      public void render(Object bound, Respond respond) {

      }

      public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return null;
      }
    };

    final PageBook pageBook = new DefaultPageBook(injector);
    pageBook.at("/wiki/:title/cat/:id", MyBrokenPageWithTemplate.class);

    PageBook.Page page = pageBook.get("/wiki/IMAX_P/cat/12");
    final MyBrokenPageWithTemplate bound = new MyBrokenPageWithTemplate();
    page.doMethod("post", bound, "/wiki/IMAX_P/cat/12", fakeRequestWithParams(new HashMap<String, String[]>()));

    assert page.widget().equals(mock);
  }

  @Test
  public final void firePostMethodOnPage() throws IOException {
    Renderable mock = new Renderable() {
      public void render(Object bound, Respond respond) {

      }

      public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return null;
      }
    };

    final PageBook pageBook = new DefaultPageBook(injector);
    pageBook.at("/wiki", MyPage.class);

    PageBook.Page page = pageBook.get("/wiki");
    final MyPage bound = new MyPage();
    page.apply(mock);
    page.doMethod("post", bound, "/wiki", fakeRequestWithParams(new HashMap<String, String[]>()));

    assert page.widget().equals(mock);
    assert bound.posted : "@Post method was not fired, on doPost()";
  }

  @DataProvider(name = URI_TEMPLATES_AND_MATCHES)
  public Object[][] getUriTemplatesAndMatches() {
    return new Object[][]{
        {"/wiki/:title", "/wiki/HelloPage"},
        {"/wiki/:title", "/wiki/HelloPage%20"},
        {"/wiki/:title/dude", "/wiki/HelloPage/dude"},
        {"/:title/thing", "/wiki/thing"},
        {"/:title", "/aposkdapoksd"},
    };
  }

  @Test(dataProvider = URI_TEMPLATES_AND_MATCHES)
  public final void matchPageByUriTemplate(final String template, final String toMatch) {
    final Respond respond = new MockRespond();

    Renderable mock = new Renderable() {
      public void render(Object bound, Respond respond) {

      }

      public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return null;
      }
    };

    final PageBook pageBook = new DefaultPageBook(injector);
    pageBook.at(template, MyPage.class);

    PageBook.Page page = pageBook.get(toMatch);
    final MyPage myPage = new MyPage();

    page.apply(mock);
    page.widget().render(myPage, respond);

    assert mock.equals(page.widget());
  }

  @DataProvider(name = NOT_URIS_AND_TEMPLATES)
  public Object[][] getNotUriTemplatesAndMatches() {
    return new Object[][]{
        {"/wiki/:title", "/tiki/HelloPage"},
        {"/wiki/:title", "/wiki/HelloPage%20/didle"},
        {"/wiki/:title/dude", "/wiki/HelloPage"},
        {"/:title/thing", "/wiki/thing/thingaling"},
        {"/:title", "/aposkdapoksd/12"},
    };
  }

  @Test(dataProvider = NOT_URIS_AND_TEMPLATES)
  public final void notMatchPageByUriTemplate(final String template, final String toMatch) {
    final PageBook pageBook = new DefaultPageBook(injector);
    pageBook.at(template, MyPage.class);

    //cant find
    assert null == pageBook.get(toMatch);

  }

  public static Request fakeRequestWithParams(Map<String, String[]> map) {
    HttpServletRequest request = createMock(HttpServletRequest.class);

    expect(request.getParameterMap()).andReturn(map);
    expect(request.getHeaderNames()).andReturn(Iterators.asEnumeration(Iterators.<Object>emptyIterator()));
    replay(request);

    return TestRequestCreator.from(request, null);
  }

  @At("/wiki")
  @Select("event")
  public static class MyEventSupportingPage {
    private boolean getted1;
    private boolean getted2;
    private boolean posted1;
    private boolean posted2;
    private boolean defaultGet;
    private boolean defaultPost;

    @Get("1")
    public void get1() {
      getted1 = true;
    }

    @Get("2")
    public void get2() {
      getted2 = true;
    }

    @Get
    public void defaultGet() {
      defaultGet = true;
    }

    @Post("1")
    public void post1() {
      posted1 = true;
    }

    @Post("2")
    public void post2() {
      posted2 = true;
    }

    @Post
    public void defaultPost() {
      defaultPost = true;
    }

  }

  @At("/wiki")
  @EmbedAs("Hi")
  public static class MyPage {
    private boolean getted;
    private boolean posted;

    @Get
    public void get() {
      getted = true;
    }

    @Post
    public void post() {
      posted = true;
    }

  }

  @At("/wiki/:title/cat/:id")
  @EmbedAs("Hi")
  public static class MyPageWithTemplate {
    private String title;
    private boolean posted;
    private String post;
    private String id;

    @Get
    public void get(@Named("title") String title) {
      this.title = title;
    }

    @Post
    public void post(@Named("title") String title, @Named("id") String id) {
      this.post = title;
      this.id = id;
    }
  }
  

  @At("/wiki/:title/cat/:int/:bool/:float/:date")
  @EmbedAs("Hi")
  public static class MyPageWithPrimitivesTemplate {
    private String title;
    private int id;
    private boolean bool;
    private Float flt;
    private Date date;

    @Get
    public void get(@Named("title") String title, @Named("int") Integer id, @Named("bool") Boolean bool, 
//    		@Named("float") float flt, @Named("date") Date date) {
            @Named("float") float flt) {
      this.title = title;
      this.id = id;
      this.bool = bool;
      this.flt = flt;
    }

  }
  
  

  @At("/wiki/:title/cat/:id")
  @EmbedAs("Hi")
  public static class MyBrokenPageWithTemplate {

    @Post
    public void post(@Named("title") String title, int x, @Named("id") String id) {
    }

  }

  @DataProvider(name = FIRST_PATH_ELEMENTS)
  public Object[][] get() {
    return new Object[][]{
        {"/wiki/:title", "wiki"},
        {"/wiki/:title/:thing", "wiki"},
        {"/wiki/other/thing/dude", "wiki"},
        {"/wiki", "wiki"},
        {"/wiki/", "wiki"},
        {"/", ""},
    };
  }

  @Test(dataProvider = FIRST_PATH_ELEMENTS)
  public final void firstPathElement(final String uri, final String answer) {
    final String fPath = new DefaultPageBook(injector)
        .firstPathElement(uri);

    assert answer.equals(fPath) : "wrong path: " + fPath;
  }

  private static class MockRespond implements Respond {

    public void write(String text) {
    }

    public HtmlTagBuilder withHtml() {
      throw new AssertionError();
    }

    public void write(char c) {
    }

    public void chew() {

    }

    public void writeToHead(String text) {

    }

    public void require(String requireString) {

    }

    public void redirect(String to) {

    }

    public String getContentType() {
      return null;
    }

    public String getRedirect() {
      return null;
    }

    public Renderable include(String argument) {
      return null;
    }

    public String getHead() {
      return null;
    }

    @Override
    public void clear() {
    }

    @Override public Object pageObject() {
      return null;
    }

    @Override
    public List<String> getErrors() {
        return null;
    }
  
    @Override
    public void setErrors(List<String> errors) {
    }
  
  }

  @At("/wiki")
  private class MyRedirectingPage {

    @Get
    public String get() {
      return REDIRECTED_GET;
    }

    @Post
    public String post() {
      return REDIRECTED_POST;
    }
  }
  
  @At("/forbidden")
  private class MyForbiddenPage {

    @Get
    public Object get() {
    	return Reply.saying().forbidden();
    }

    @Post
    public Object post() {
      return Reply.saying().forbidden();
    }
  }
}
