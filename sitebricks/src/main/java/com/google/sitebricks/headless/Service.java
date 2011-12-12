package com.google.sitebricks.headless;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Analogous to {@literal @}Show but representing a headless (page-less)
 * web service. Sometimes called a Restful web service. This kind of
 * web page has no corresponding template file and the page class returns
 * a response directly.
 * <p>
 * By default, the service annotation will execute for any valid method
 * bound at the specified URL (with the @At annotation or {@code at()}
 * module method).
 * <p>
 * However, it is possible to selectively dispatch services by specifying a
 * value:
 * <pre>
 *
 *  {@literal @}At("/doc"){@literal @}Service("fetch")
 *   public class FetchDoc { .. }
 * </pre>
 *
 * In this scenario, all requests to "/doc" will be tested for the special
 * request parameter "r". If "r" contains "fetch", then {@code FetchDoc} will
 * execute, otherwise it will be ignored.
 * <p>
 * The special request parameter "r" is a comma-separated list of service endpoints
 * that a browser-client wishes to dispatch. This enables several convenient
 * programming models:
 * <ul>
 *   <li>RPC tunneling over HTTP</li>
 *   <li>Batching multiple operations over a single HTTP request</li>
 *   <li>Request fan-out (you can more easily service a single request
 *       from various backends)</li>
 *   <li>More responsive AJAX UIs</li>
 * </ul>
 * <p>
 * The special request parameter "r" can be customized via your {@code SitebricksModule}
 * to be anything you like. If no endpoints match the request (i.e. "r=" is empty or
 * corrupt), then a 405 (Method Not Allowed) error is returned.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
  String value() default "";
}
