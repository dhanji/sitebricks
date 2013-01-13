package com.google.sitebricks.routing;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.sitebricks.Respond;
import com.google.sitebricks.StringBuilderRespond;
import com.google.sitebricks.binding.FlashCache;
import com.google.sitebricks.binding.RequestBinder;
import com.google.sitebricks.headless.HeadlessRenderer;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.rendering.resource.ResourcesService;
import com.google.sitebricks.routing.PageBook.Page;
import net.jcip.annotations.Immutable;

import java.io.IOException;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Immutable
@Singleton
class WidgetRoutingDispatcher implements RoutingDispatcher {
  private final PageBook book;
  private final RequestBinder binder;
  private final ResourcesService resourcesService;
  private final Provider<FlashCache> flashCacheProvider;
  private final HeadlessRenderer headlessRenderer;

  @Inject
  public WidgetRoutingDispatcher(PageBook book, RequestBinder binder,
                                 ResourcesService resourcesService,
                                 Provider<FlashCache> flashCacheProvider,
                                 HeadlessRenderer headlessRenderer) {
    this.headlessRenderer = headlessRenderer;
    this.book = book;
    this.binder = binder;
    this.resourcesService = resourcesService;
    this.flashCacheProvider = flashCacheProvider;
  }

  public Object dispatch(Request request, Events event) throws IOException {
    String uri = request.path();

    //first try dispatching as a static resource service
    Respond respond = resourcesService.serve(uri);

    if (null != respond)
      return respond;

    // Otherwise try to dispatch as a widget/page
    // Check if there is a page chain link sitting here
    // for this page.
    // NOTE(dhanji): we must use remove, to atomically
    // remove the page and process it in one go. It is
    // also worth coordinating this with conversation request
    // queueing.
    // TODO(dhanji): Change flashcache to use temporary cookies instead.
    PageBook.Page page = flashCacheProvider.get().remove(uri);

    // If there is no link, obtain page via Guice as normal.
    if (null == page)
      page = book.get(uri);

    //could not dispatch as there was no match
    if (null == page)
      return null;

    final Object instance = page.instantiate();
    if (page.isHeadless()) {
      return bindAndReply(request, page, instance);
    } else {
       //fire events and render reponders
      return bindAndRespond(request, page, instance);
    }
  }

  private Object bindAndReply(Request request, Page page, Object instance) throws IOException {
    // bind request (sets request params, etc).
    binder.bind(request, instance);

    // call the appropriate handler.
    return fireEvent(request, page, instance);
  }

  private Object bindAndRespond(Request request, PageBook.Page page, Object instance)
      throws IOException {
    //bind request
    binder.bind(request, instance);

    //fire get/post events
    final Object redirect = fireEvent(request, page, instance);

    //render to respond
    Respond respond = new StringBuilderRespond(instance);
    if (null != redirect) {

      if (redirect instanceof String)
        respond.redirect((String) redirect);
      else if (redirect instanceof Class) {
        PageBook.Page targetPage = book.forClass((Class<?>) redirect);

        // should never be null coz it is validated on compile.
        respond.redirect(contextualize(request, targetPage.getUri()));
      } else if (redirect instanceof Reply<?>) {
    	  // To allow non-headless pages to use Reply<?> for more advanced HTTP responses
    	  return redirect;
      } else {
        // Handle page-chaining driven redirection.
        PageBook.Page targetPage = book.forInstance(redirect);

        // should never be null coz it will be validated at compile time.
        flashCacheProvider.get().put(targetPage.getUri(), targetPage);

        // Send to the canonical address of the page. This is also
        // verified at compile, not be a variablized matcher.
        respond.redirect(contextualize(request, targetPage.getUri()));
      }
    } else {
      page.widget().render(instance, respond);
    }
    
    return respond;
  }

  // We're sure the request parameter map is a Map<String, String[]>
  @SuppressWarnings("unchecked")
  private Object fireEvent(Request request, PageBook.Page page, Object instance)
      throws IOException {
    final String method = request.method();
    final String pathInfo = request.path();

    return page.doMethod(method.toLowerCase(), instance, pathInfo, request);
  }

  private static String contextualize(Request request, String targetUri) {
    return request.context() + targetUri;
  }
}
