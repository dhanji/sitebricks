package com.google.inject.stat;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.inject.stat.StatsServlet.DEFAULT_FORMAT;

import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.servlet.ServletModule;
import com.google.inject.stat.StatsPublishers.HtmlStatsPublisher;
import com.google.inject.stat.StatsPublishers.JsonStatsPublisher;
import com.google.inject.stat.StatsPublishers.TextStatsPublisher;

/**
 * This module enables publishing values annotated with {@link Stat} to a given
 * servlet path.
 * <p>
 * <h3>Example of Use</h3>
 * As an example, consider the following class:
 * <pre><code>
 * class QueryServlet extends HttpServlet {
 *    {@literal @}Stat("search-hits")
 *     private final AtomicInteger hits = new AtomicInteger(0);
 *
 *    {@literal @}Inject QueryServlet(....) { }
 *
 *    {@literal @}Override void doGet(
 *        HttpServletRequest req, HttpServletResponse resp) {
 *      ....
 *      String searchTerm = req.getParameter("q");
 *      SearchResult result = searchService.searchFor(searchTerm);
 *      if (result.hasHits()) {
 *        hits.incrementAndGet();
 *      }
 *      ...
 *    }
 * }
 * </pre></code>
 * <p>
 * This class registers a stat called {@code search-hits}.  To configure the
 * server to publish this stat, install a {@link StatModule}, such as:
 * <pre><code>
 * public class YourServerModule extends AbstractModule {
 *  {@literal @}Override protected void configure() {
 *    install(new StatsModule("/stats");
 *    ...
 *   }
 * }
 * </code></pre>
 * <p>
 * Then, to query the server for its stats, hit the url that was registered
 * with the module (which was {@code /stats}, in the example above).
 *
 * <h3>Registering Stats</h3>
 * The simplest way of registering a stat is to use the <code>@Stat</code>
 * annotation.  Members of a class annotated by <code>@Stat</code> are
 * registered automatically when an instance of the class is created by Guice.
 * The value of the member is read when a snapshot of the stats is requested,
 * most likely by the {@link StatsServlet} upon a request to {@code /stats}.
 * <p>
 * At times it is convenient to "manually" register a stat.  To do this,
 * inject an instance of {@link StatRegistrar} and use it to register a stat.
 * For example:
 * <pre><code>
 * class RegistersLocalVariableAsStat {
 *
 *   private final StatRegistrar statRegistrar;
 *
 *   {@literal @}Inject RegistersLocalVariableAsStat(
 *       StatRegistrar statRegistrar) {
 *     this.statRegistrar = statRegistrar;
 *    }
 *
 *   void initialize() {
 *     long start = System.currentTimeMillis();
 *     doInitialization();
 *     statRegistrar.registerSingleStat(
 *       "init-time-in-ms",
 *       "Initialization time of a class",
 *       System.currentTimeMillis() - start);
 *   }
 * }
 * </code></pre>
 * There are other convenience methods on {@link StatRegistrar} to facilitate
 * registering annotated static members on classes and registering all
 * annotated members on instances as well.
 *
 * <h3>Exposing Stats</h3>
 * It's important to consider, if only to be careful, how to prevent a mutable
 * reference of a stat from leaking into the stat publishing logic.  For
 * instance, if you were to publish a deeply mutable reference to a
 * <code>List</code>, then a stat publisher could inadvertently (or purposely)
 * mutate it.
 * <p>
 * It is the role of a {@link StatExposer} to guard against such leaks:  An
 * exposer is given the raw value of a stat, and should return a safe view of
 * it.  This view is then passed to the {@link StatsPublishers publishers}.
 * <p>
 * By default, a {@link StatExposers.InferenceExposer} is used to guard stats
 * registered via {@link Stat <code>@Stat</code>}.  This implementation should
 * handle the majority of common use cases.  If, however, you want to use a
 * different {@link StatExposer} for your stat, then you may do so by
 * specifying its class within the {@link Stat <code>@Stat</code>} annotation.
 * For example:
 * <pre><code>
 * class ServiceStat implements Cloneable {
 *   int calls;
 *   AtomicLong&lt;Long&gt; latencyInMs;
 *
 *  {@literal @}Override protected Object clone() {
 *     return new ServiceStat(calls, latencyInMs);
 *   }
 * }
 *
 * class ServiceStatExposer implements StatExposer&lt;ServiceStat&gt; {
 *  {@literal @}Override Object expose(ServiceStat serviceStat) {
 *     return serviceStat.clone();
 *   }
 * }
 *
 * class Service {
 *  {@literal @}Stat(value = "service-stat", exposer = ServiceStatExposer.class)
 *   private final ServiceStat serviceStat;
 *
 *   ...
 * }
 * </pre></code>
 *
 * <h3>Published Formats</h3>
 * By default, published stats are available in several formats:
 * <ul>
 *  <li>html - a formatted html page
 *  <li>json - well formed json
 *  <li>text - simple plaintext page
 * </ul>
 * To request stats in a given format, include a value for the
 * {@value StatsServlet#DEFAULT_FORMAT} parameter in the {@code /stats} request.
 * For the formats above, the value for this parameter should correspond to the
 * type of output (i.e., pass "html", "json", or "text").  If no parameter is
 * given, then html is returned.
 * <p>
 * <h3>Extensions</h3>
 * You may extend the default set of publishers by adding and binding another
 * implementation of {@link StatsPublisher}.  To add your implementation,
 * add a binding to a {@link MapBinder MapBinder&lt;String, StatsPublisher&gt;}.
 * For example:
 * <pre><code>
 * public class CustomPublisherModule extends AbstractModule {
 *   {@literal @}Override protected void configure() {
 *      MapBinder&lt;String, StatsPublisher&gt; mapBinder =
 *         MapBinder.newMapBinder(binder(), String.class, StatsPublisher.class);
 *      mapBinder.addBinding("custom").to(CustomStatsPublisher.class);
 *   }
 * }
 * </code></pre>
 * You can then retrieve stats from your custom publisher by hitting
 * {@code /stats?format=custom}.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 * @author ffaber@gmail.com (Fred Faber)
 */
public class StatModule extends ServletModule {
  private final String uriPath;

  public StatModule(String uriPath) {
    checkArgument(!isNullOrEmpty(uriPath),
        "URI path must be a valid non-empty servlet path mapping (example: /stats)");
    this.uriPath = uriPath;
  }

  @Override
  protected void configureServlets() {
    // Manual bootstrapping is needed to instantiated a well-formed listener.
    Stats stats = new Stats();
    bind(Stats.class).toInstance(stats);
    requestInjection(stats);

    StatRegistrar statRegistrar = new StatRegistrar(stats);
    bind(StatRegistrar.class).toInstance(statRegistrar);

    StatAnnotatedTypeListener listener =
        new StatAnnotatedTypeListener(statRegistrar);

    bindListener(Matchers.any(), listener);

    serve(uriPath).with(StatsServlet.class);

    MapBinder<String, StatsPublisher> publisherBinder =
        MapBinder.newMapBinder(binder(), String.class, StatsPublisher.class);
    publisherBinder.addBinding(DEFAULT_FORMAT).to(HtmlStatsPublisher.class);
    publisherBinder.addBinding("html").to(HtmlStatsPublisher.class);
    publisherBinder.addBinding("json").to(JsonStatsPublisher.class);
    publisherBinder.addBinding("text").to(TextStatsPublisher.class);
  }
}
