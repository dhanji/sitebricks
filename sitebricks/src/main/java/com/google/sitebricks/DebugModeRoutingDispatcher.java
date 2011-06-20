package com.google.sitebricks;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.sitebricks.compiler.TemplateCompileException;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.routing.PageBook;
import com.google.sitebricks.routing.Production;
import com.google.sitebricks.routing.RoutingDispatcher;
import com.google.sitebricks.routing.SystemMetrics;
import net.jcip.annotations.ThreadSafe;
import org.mvel2.PropertyAccessException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

/**
 * In debug mode, this dispatcher is used to intercept the production dispatcher and provide debug
 * services (such as the /debug page, and the friendly compile errors page).
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@ThreadSafe
@Singleton
class DebugModeRoutingDispatcher implements RoutingDispatcher {
  private final RoutingDispatcher dispatcher;
  private final SystemMetrics metrics;
  private final PageBook pageBook;

  @Inject
  public DebugModeRoutingDispatcher(@Production RoutingDispatcher dispatcher,
                                    SystemMetrics metrics,
                                    PageBook pageBook) {

    this.dispatcher = dispatcher;
    this.metrics = metrics;
    this.pageBook = pageBook;
  }


  public Object dispatch(Request request, Events event)
      throws IOException {
    long start = System.currentTimeMillis();

    // Attempt to discover page class.
    final PageBook.Page page = pageBook.get(request.path());

    // This may be a static resource (in which case we dont gather metrics for it).
    Class<?> pageClass = null;
    if (null != page)
      pageClass = page.pageClass();

    try {
      return dispatcher.dispatch(request, Events.DURING);


    } catch (TemplateCompileException tce) {
      // NOTE(dhanji): Don't log error metrics here, they are better handled by the compiler.

      final Respond respond = new StringBuilderRespond(new Object());

      respond.write("<h3>");
      respond.write("Compile errors in page");
      respond.write("</h3>");
      respond.write("<pre>");
      respond.write(tce.getMessage());
      respond.write("</pre>");
      respond.write("<br/>");
      respond.write("<br/>");
      respond.write("<br/>");

      return respond;


    } catch (PropertyAccessException pae) {
      final Respond respond = new StringBuilderRespond(new Object());

      Throwable cause = pae.getCause();

      respond.write("<h3>");
      respond.write("Exception during page render");
      respond.write("</h3>");
      respond.write("<br/>");
      respond.write("<br/>");
      respond.write("<br/>");

      // Analyze cause and construct a detailed error report.
      if (cause instanceof InvocationTargetException) {
        InvocationTargetException ite = (InvocationTargetException) cause;
        cause = ite.getCause();
      }

      if (cause == null)
        cause = pae;

      // Create ourselves a printwriter to buffer error output into.
      final StringWriter writer = new StringWriter();
      cause.printStackTrace(new PrintWriter(writer));

      respond.write("<h3>");
      respond.write("Exception during page render");
      respond.write("</h3>");
      respond.write("<pre>");
      respond.write(writer.toString());
      respond.write("</pre>");

      return respond;
    } finally {
      long time = System.currentTimeMillis() - start;

      // Only log time metric if this is a dynamic resource.
      if (null != pageClass)
        metrics.logPageRenderTime(pageClass, time);
    }
  }
}
