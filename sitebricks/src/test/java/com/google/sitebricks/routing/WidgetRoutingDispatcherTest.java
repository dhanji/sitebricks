package com.google.sitebricks.routing;

import com.google.inject.Provider;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import com.google.sitebricks.TestRequestCreator;
import com.google.sitebricks.binding.FlashCache;
import com.google.sitebricks.binding.RequestBinder;
import com.google.sitebricks.headless.HeadlessRenderer;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.rendering.resource.ResourcesService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.matches;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class WidgetRoutingDispatcherTest {
  private static final String REDIRECTED_POST = "/redirect_post";
  private static final String REDIRECTED_GET = "/redirect_get";
  private static final String A_STATIC_RESOURCE_URI = "/not_thing";

  private Provider<FlashCache> flashCacheProvider;
  private FlashCache flashCache;

  private HttpServletResponse response;

  @BeforeMethod
  @SuppressWarnings("unchecked")
  public final void initFlashCacheProvider() {
    flashCacheProvider = createMock(Provider.class);
    flashCache = createMock(FlashCache.class);

    expect(flashCacheProvider.get())
        .andReturn(flashCache).anyTimes();

    replay(flashCacheProvider);

    response = createNiceMock(HttpServletResponse.class);
  }

  @Test
  public final void dispatchRequestAndRespondOnGet() throws IOException {
    final HttpServletRequest request = createNiceMock(HttpServletRequest.class);
    PageBook pageBook = createMock(PageBook.class);
    PageBook.Page page = createMock(PageBook.Page.class);
    Renderable widget = createMock(Renderable.class);
    final Respond respond = createMock(Respond.class);
    RequestBinder binder = createMock(RequestBinder.class);

    Object pageOb = new Object();


    expect(request.getRequestURI())
        .andReturn("/thing")
        .anyTimes();

    expect(request.getContextPath())
        .andReturn("")
        .anyTimes();

    expect(request.getParameterMap())
        .andReturn(new HashMap())
        .anyTimes();

    expect(pageBook.get("/thing"))
        .andReturn(page);

    binder.bind(isA(Request.class), same(pageOb));
    expectLastCall().once();

    expect(page.isHeadless())
        .andReturn(false);

    expect(page.widget())
        .andReturn(widget);

    expect(page.instantiate())
        .andReturn(pageOb);

    expect(request.getMethod())
        .andReturn("GET");

    expect(page.doMethod(eq("get"), same(pageOb), eq("/thing"), isA(Request.class)))
        .andReturn(null);


    widget.render(pageOb, respond);
    expectLastCall().once();


    replay(request, page, pageBook, widget, respond, binder);

    Respond out = (Respond) new WidgetRoutingDispatcher(pageBook, binder, new Provider<Respond>() {
      public Respond get() {
        return respond;
      }
    }, createNiceMock(ResourcesService.class), flashCacheProvider,
        createNiceMock(HeadlessRenderer.class)).dispatch(TestRequestCreator.from(request, null)
    );


    assert out == respond : "Did not respond correctly";

    verify(request, page, pageBook, widget, respond, binder);

  }

  @Test
  public final void dispatchRequestToCorrectEventHandlerOnGet() throws IOException {
    final HttpServletRequest request = createNiceMock(HttpServletRequest.class);
    PageBook pageBook = createMock(PageBook.class);
    PageBook.Page page = createMock(PageBook.Page.class);
    Renderable widget = createMock(Renderable.class);
    final Respond respond = createMock(Respond.class);
    RequestBinder binder = createMock(RequestBinder.class);

    Object pageOb = new Object();

    expect(request.getRequestURI())
        .andReturn("/thing")
        .anyTimes();

    expect(request.getContextPath())
        .andReturn("")
        .anyTimes();

    expect(pageBook.get("/thing"))
        .andReturn(page);

    binder.bind(isA(Request.class), same(pageOb)); 
    expectLastCall().once();


    expect(page.isHeadless())
        .andReturn(false);

    expect(page.widget())
        .andReturn(widget);

    expect(page.instantiate())
        .andReturn(pageOb);

    expect(request.getMethod())
        .andReturn("GET");

    final HashMap<String, String[]> parameterMap = new HashMap<String, String[]>();
    expect(request.getParameterMap())
        .andReturn(parameterMap)
        .anyTimes();

    expect(page.doMethod(eq("get"), same(pageOb), eq("/thing"), isA(Request.class)))
        .andReturn(null);

    widget.render(pageOb, respond);
    expectLastCall().once();


    replay(request, page, pageBook, widget, respond, binder);

    Respond out = (Respond) new WidgetRoutingDispatcher(pageBook, binder, new Provider<Respond>() {
      public Respond get() {
        return respond;
      }
    }, createNiceMock(ResourcesService.class), flashCacheProvider,
        createNiceMock(HeadlessRenderer.class)).dispatch(TestRequestCreator.from(request, null)
    );


    assert out == respond : "Did not respond correctly";

    verify(request, page, pageBook, widget, respond, binder);

  }


  @Test
  public final void dispatchRequestAndRespondOnPost() throws IOException {
    final HttpServletRequest request = createNiceMock(HttpServletRequest.class);
    PageBook pageBook = createMock(PageBook.class);
    PageBook.Page page = createMock(PageBook.Page.class);
    Renderable widget = createMock(Renderable.class);
    final Respond respond = createMock(Respond.class);
    RequestBinder binder = createMock(RequestBinder.class);

    Object pageOb = new Object();

    expect(request.getRequestURI())
        .andReturn("/thing")
        .anyTimes();

    expect(request.getContextPath())
        .andReturn("")
        .anyTimes();

    expect(request.getParameterMap())
        .andReturn(new HashMap())
        .anyTimes();

    expect(pageBook.get("/thing"))
        .andReturn(page);

    binder.bind(isA(Request.class), same(pageOb));
    expectLastCall().once();


    expect(page.isHeadless())
        .andReturn(false);

    expect(page.widget())
        .andReturn(widget);

    expect(page.instantiate())
        .andReturn(pageOb);

    expect(request.getMethod())
        .andReturn("POST");

    //noinspection unchecked
    expect(page.doMethod(matches("post"), eq(pageOb), eq("/thing"), isA(Request.class)))
        .andReturn(null);
//        expectLastCall().once();


    widget.render(pageOb, respond);
    expectLastCall().once();


    replay(request, page, pageBook, widget, respond, binder);

    Respond out = (Respond) new WidgetRoutingDispatcher(pageBook, binder, new Provider<Respond>() {
      public Respond get() {
        return respond;
      }
    }, createNiceMock(ResourcesService.class), flashCacheProvider,
        createNiceMock(HeadlessRenderer.class)).dispatch(TestRequestCreator.from(request, null)
    );


    assert out == respond : "Did not respond correctly";

    verify(request, page, pageBook, widget, respond, binder);

  }

  @Test
  public final void dispatchRequestAndRedirectOnPost() throws IOException {
    final HttpServletRequest request = createNiceMock(HttpServletRequest.class);
    PageBook pageBook = createMock(PageBook.class);
    PageBook.Page page = createMock(PageBook.Page.class);
    Renderable widget = createMock(Renderable.class);
    final Respond respond = createMock(Respond.class);
    RequestBinder binder = createMock(RequestBinder.class);

    Object pageOb = new Object();

    expect(request.getRequestURI())
        .andReturn("/thing")
        .anyTimes();

    expect(request.getContextPath())
        .andReturn("")
        .anyTimes();

    expect(request.getParameterMap())
        .andReturn(new HashMap())
        .anyTimes();

    expect(pageBook.get("/thing"))
        .andReturn(page);

    binder.bind(isA(Request.class), same(pageOb));
    expectLastCall().once();

    expect(page.isHeadless())
        .andReturn(false);

    expect(page.instantiate())
        .andReturn(pageOb);

    expect(request.getMethod())
        .andReturn("POST");

    respond.redirect(REDIRECTED_POST);

    //noinspection unchecked
    expect(page.doMethod(matches("post"), eq(pageOb), eq("/thing"), isA(Request.class)))
        .andReturn(REDIRECTED_POST);


//        widget.render(pageOb, respond);
//        expectLastCall().once();


    replay(request, page, pageBook, widget, respond, binder);

    Respond out = (Respond) new WidgetRoutingDispatcher(pageBook, binder, new Provider<Respond>() {
      public Respond get() {
        return respond;
      }
    }, createNiceMock(ResourcesService.class), flashCacheProvider,
        createNiceMock(HeadlessRenderer.class)).dispatch(TestRequestCreator.from(request, null)
    );


    assert out == respond : "Did not respond correctly";

    verify(request, page, pageBook, widget, respond, binder);

  }

  @Test
  public final void dispatchRequestAndRedirectOnGet() throws IOException {
    final HttpServletRequest request = createNiceMock(HttpServletRequest.class);
    PageBook pageBook = createMock(PageBook.class);
    PageBook.Page page = createMock(PageBook.Page.class);
    Renderable widget = createMock(Renderable.class);
    final Respond respond = createMock(Respond.class);
    RequestBinder binder = createMock(RequestBinder.class);

    Object pageOb = new Object();


    expect(request.getRequestURI())
        .andReturn("/thing")
        .anyTimes();

    expect(request.getContextPath())
        .andReturn("")
        .anyTimes();

    expect(request.getParameterMap())
        .andReturn(new HashMap())
        .anyTimes();

    expect(pageBook.get("/thing"))
        .andReturn(page);

    binder.bind(isA(Request.class), same(pageOb));
    expectLastCall().once();

    expect(page.isHeadless())
        .andReturn(false);

    expect(page.instantiate())
        .andReturn(pageOb);

    expect(request.getMethod())
        .andReturn("GET");

    respond.redirect(REDIRECTED_GET);

    //noinspection unchecked
    expect(page.doMethod(matches("get"), eq(pageOb), eq("/thing"), isA(Request.class)))
        .andReturn(REDIRECTED_GET);


//        widget.render(pageOb, respond);
//        expectLastCall().once();


    replay(request, page, pageBook, widget, respond, binder);

    Respond out = (Respond) new WidgetRoutingDispatcher(pageBook, binder, new Provider<Respond>() {
      public Respond get() {
        return respond;
      }
    }, createNiceMock(ResourcesService.class), flashCacheProvider,
        createNiceMock(HeadlessRenderer.class)).dispatch(TestRequestCreator.from(request, null)
    );


    assert out == respond : "Did not respond correctly";

    verify(request, page, pageBook, widget, respond, binder);

  }

  @Test
  public final void dispatchNothingBecauseOfNoUriMatch() throws IOException {
    final HttpServletRequest request = createNiceMock(HttpServletRequest.class);
    PageBook pageBook = createMock(PageBook.class);
    RequestBinder binder = createMock(RequestBinder.class);

    @SuppressWarnings("unchecked")
    Provider<Respond> respond = createMock(Provider.class);

    expect(request.getRequestURI())
        .andReturn(A_STATIC_RESOURCE_URI)
        .anyTimes();

    expect(request.getContextPath())
        .andReturn("")
        .anyTimes();

    expect(pageBook.get(A_STATIC_RESOURCE_URI))
        .andReturn(null);

    replay(request, pageBook, respond, binder);

    Respond out = (Respond) new WidgetRoutingDispatcher(pageBook, binder, respond,
        createNiceMock(ResourcesService.class),
        flashCacheProvider,
        createNiceMock(HeadlessRenderer.class)).dispatch(TestRequestCreator.from(request, null)
    );


    assert out == null : "Did not respond correctly";

    verify(request, pageBook, respond, binder);

  }

  @Test
  public final void dispatchStaticResource() throws IOException {
    final HttpServletRequest request = createNiceMock(HttpServletRequest.class);
    PageBook pageBook = createMock(PageBook.class);
    RequestBinder binder = createMock(RequestBinder.class);
    ResourcesService resourcesService = createMock(ResourcesService.class);
    Respond mockRespond = createMock(Respond.class);

    @SuppressWarnings("unchecked")
    Provider<Respond> respond = createMock(Provider.class);


    expect(request.getRequestURI())
        .andReturn(A_STATIC_RESOURCE_URI)
        .anyTimes();

    expect(request.getContextPath())
        .andReturn("")
        .anyTimes();

    expect(resourcesService.serve(A_STATIC_RESOURCE_URI))
        .andReturn(mockRespond);

    replay(request, pageBook, respond, binder, resourcesService);

    Respond out = (Respond) new WidgetRoutingDispatcher(pageBook, binder, respond, resourcesService,
        flashCacheProvider, createNiceMock(HeadlessRenderer.class)).dispatch(
        TestRequestCreator.from(request, null));


    assert out != null : "Did not respond correctly";
    assert mockRespond.equals(out);

    verify(request, pageBook, respond, binder, resourcesService);

  }
}
