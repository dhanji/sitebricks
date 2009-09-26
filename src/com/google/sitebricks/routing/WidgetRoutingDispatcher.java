package com.google.sitebricks.routing;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.sitebricks.Respond;
import com.google.sitebricks.binding.FlashCache;
import com.google.sitebricks.binding.RequestBinder;
import com.google.sitebricks.rendering.resource.ResourcesService;
import net.jcip.annotations.Immutable;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Immutable
@Singleton
class WidgetRoutingDispatcher implements RoutingDispatcher {
  private final PageBook book;
  private final RequestBinder binder;
  private final Provider<Respond> respondProvider;
  private final ResourcesService resourcesService;
  private final Provider<FlashCache> flashCache;

  @Inject
  public WidgetRoutingDispatcher(PageBook book, RequestBinder binder, Provider<Respond> respondProvider,
                                 ResourcesService resourcesService, Provider<FlashCache> flashCache) {
    this.book = book;
    this.binder = binder;
    this.respondProvider = respondProvider;
    this.resourcesService = resourcesService;
    this.flashCache = flashCache;
  }

  public Respond dispatch(HttpServletRequest request) {
    String uri = getPathInfo(request);

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
    PageBook.Page page = flashCache.get().remove(uri);

    // If there is no link, obtain page via Guice as normal.
    if (null == page)
      page = book.get(uri);

    //could not dispatch as there was no match
    if (null == page)
      return null;

    respond = respondProvider.get();
    final Object instance = page.instantiate();

    //fire events and render reponders
    bindAndRespond(request, page, respond, instance);

    return respond;
  }

  private String getPathInfo(HttpServletRequest request) {
    return request.getRequestURI().substring(request.getContextPath().length());
  }

  private void bindAndRespond(HttpServletRequest request, PageBook.Page page, Respond respond,
                              Object instance) {
    //bind request
    binder.bind(request, instance);

    //fire get/post events
    final Object redirect = fireEvent(request, page, instance);

    //render to respond
    if (null != redirect) {

      if (redirect instanceof String)
        respond.redirect((String) redirect);
      else {
        // Handle page-chaining driven redirection.
        PageBook.Page targetPage = book.forInstance(redirect);

        // should never be null coz it will be validated at compile time.
        flashCache.get().put(targetPage.getUri(), targetPage);

        // Send to the canonical address of the page. This is also
        // verified at compile, not be a variablized matcher.
        respond.redirect(contextualize(request, targetPage.getUri()));
      }
    } else
      page.widget().render(instance, respond);
  }

  // We're sure the request parameter map is a Map<String, String[]>
  @SuppressWarnings("unchecked")
  private Object fireEvent(HttpServletRequest request, PageBook.Page page, Object instance) {
    final String method = request.getMethod();
    final String pathInfo = getPathInfo(request);

    return page.doMethod(method.toLowerCase(), instance, pathInfo, request.getParameterMap());
  }

  private static String contextualize(HttpServletRequest request, String targetUri) {
    return request.getContextPath() + targetUri;
  }
}
