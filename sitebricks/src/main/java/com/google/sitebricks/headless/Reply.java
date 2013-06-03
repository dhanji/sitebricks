package com.google.sitebricks.headless;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.sitebricks.client.Transport;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Reply to a (headless) web request.
 */
public abstract class Reply<E> {
  // Asks sitebricks to continue down the servlet processing chain
  public static final Reply<?> NO_REPLY = Reply.saying();

  public static final String NO_REPLY_ATTR = "sb_no_reply";

  /**
   * Perform a 301 redirect (moved permanently) to the given uri.
   */
  public abstract Reply<E> seeOther(String uri);

  /**
   * Perform a custom redirect to the given uri. The status code must be
   * in the 3XX range.
   */
  public abstract Reply<E> seeOther(String uri, int statusCode);

  /**
   * The media type of the response data to send to the client. I.e.
   * the mime-type. Example {@code "application/json"} for JSON responses.
   */
  public abstract Reply<E> type(String mediaType);

  /**
   * A Map of headers to set directly on the response.
   */
  public abstract Reply<E> headers(Map<String, String> headers);

  /**
   * Perform a 404 not found reply.
   */
  public abstract Reply<E> notFound();

  /**
   * Perform a 401 not authorized reply.
   */
  public abstract Reply<E> unauthorized();

  /**
   * Directs sitebricks to use the given Guice key as a transport to
   * marshall the provided entity to the client. Example:
   * <pre>
   *   return Reply.with(new Person(..)).as(Xml.class);
   * </pre>
   * <p>
   * Will marhall the given Person object into XML using the Guice Key
   * bound to [Xml.class] (by default this is an XStream based XML
   * transport).
   */
  public abstract Reply<E> as(Class<? extends Transport> transport);

  /**
   * Same as {@link #as(Class)}.
   */
  public abstract Reply<E> as(Key<? extends Transport> transport);

  /**
   * Perform a 302 redirect to the given uri (moved temporarily).
   */
  public abstract Reply<E> redirect(String uri);

  /**
   * Perform a 403 resource forbidden error response.
   */
  public abstract Reply<E> forbidden();

  /**
   * Perform a 204 no content response.
   */
  public abstract Reply<E> noContent();

  /**
   * Perform a 500 general error response.
   */
  public abstract Reply<E> error();

  /**
   * Perform a 400 Bad Request response.
   */
  public abstract Reply<E> badRequest();

  /**
   * Render template associated with the given class. The class must have
   * an @Show() annotation pointing to a valid Sitebricks template type (can
   * be any of the supported templates: MVEL, freemarker, SB, etc.)
   * <p>
   * The entity passed into with() is used as the template's context during
   * render.
   */
  public abstract Reply<E> template(Class<?> templateKey);

  /**
   * Set a custom status code (call this last, it will be overridden if
   * other response code directives are called afterward).
   */
  public abstract Reply<E> status(int code);

  /**
   * Perform a 200 OK response with no body.
   */
  public abstract Reply<E> ok();

  /**
   * Used internally by sitebricks. Do NOT call.
   */
  abstract void populate(Injector injector, HttpServletResponse response) throws IOException;

  /**
   * Convenience method to make a reply without any entity or body. Example, to send a redirect:
   * <pre>
   *   return Reply.saying().redirect("/other");
   * </pre>
   */
  public static <E> Reply<E> saying() {
    return new ReplyMaker<E>(null);
  }

  /**
   * Returns a reply with an entity that is sent back to the client via the specified
   * transport.
   *
   * @param entity An entity to send back for which a valid transport exists (see
   *   {@link #as(Class)}).
   */
  public static <E> Reply<E> with(E entity) {
    return new ReplyMaker<E>(entity);
  }

}
