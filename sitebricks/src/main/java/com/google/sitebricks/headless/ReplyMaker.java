package com.google.sitebricks.headless;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.sitebricks.client.Transport;
import com.google.sitebricks.client.transport.Text;
import com.google.sitebricks.rendering.Strings;
import com.google.sitebricks.rendering.Templates;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * A builder implementation of the Reply interface.
 */
class ReplyMaker<E> extends Reply<E> {

  // By default, we cool.
  int status = HttpServletResponse.SC_OK;

  String contentType;

  String redirectUri;
  Map<String, String> headers = Maps.newHashMap();

  Key<? extends Transport> transport = Key.get(Text.class);
  E entity;
  Class<?> templateKey;

  public ReplyMaker(E entity) {
    this.entity = entity;
  }

  @Override
  public Reply<E> seeOther(String uri) {
    redirectUri = uri;
    status = HttpServletResponse.SC_MOVED_PERMANENTLY;
    return this;
  }

  @Override
  public Reply<E> seeOther(String uri, int statusCode) {
    Preconditions.checkArgument(statusCode >= 300 && statusCode < 400,
        "Redirect statuses must be between 300-399");
    redirectUri = uri;
    status = statusCode;
    return this;
  }

  @Override
  public Reply<E> type(String mediaType) {
    Strings.nonEmpty(mediaType, "Media type cannot be null or empty");
    this.contentType = mediaType;
    return this;
  }

  @Override
  public Reply<E> headers(Map<String, String> headers) {
    this.headers.putAll(headers);
    return this;
  }

  @Override
  public Reply<E> notFound() {
    status = HttpServletResponse.SC_NOT_FOUND;
    return this;
  }

  @Override
  public Reply<E> unauthorized() {
    status = HttpServletResponse.SC_UNAUTHORIZED;
    return this;
  }

  @Override
  public Reply<E> as(Key<? extends Transport> transport) {
    Preconditions.checkArgument(null != transport, "Transport class cannot be null!");
    this.transport = transport;
    return this;
  }

  @Override
  public Reply<E> as(Class<? extends Transport> transport) {
    Preconditions.checkArgument(null != transport, "Transport class cannot be null!");
    this.transport = Key.get(transport);
    return this;
  }

  @Override
  public Reply<E> redirect(String url) {
    Strings.nonEmpty(url, "Redirect URL must be non empty!");
    this.redirectUri = url;
    status = HttpServletResponse.SC_MOVED_TEMPORARILY;
    return this;
  }

  @Override
  public Reply<E> forbidden() {
    status = HttpServletResponse.SC_FORBIDDEN;
    return this;
  }

  @Override
  public Reply<E> noContent() {
    status = HttpServletResponse.SC_NO_CONTENT;
    return this;
  }

  @Override
  public Reply<E> error() {
    status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    return this;
  }

  @Override
  public Reply<E> status(int code) {
    status = code;
    return this;
  }

  @Override
  public Reply<E> ok() {
    status = HttpServletResponse.SC_OK;
    return this;
  }

  @Override
  public Reply<E> template(Class<?> templateKey) {
    this.templateKey = templateKey;
    return this;
  }

  @Override
  void populate(Injector injector, HttpServletResponse response) throws IOException {
    // If we should not bother with the chain
    if (Reply.NO_REPLY == this) {
      injector.getInstance(HttpServletRequest.class).setAttribute(Reply.NO_REPLY_ATTR, Boolean.TRUE);
      return;
    }

    // This is where we take all the builder values and encode them in the response.
    Transport transport = injector.getInstance(this.transport);

    // Set any headers (we do this first, so we can override any cheekily set headers).
    if (!headers.isEmpty()) {
      for (Map.Entry<String, String> header : headers.entrySet()) {
        response.setHeader(header.getKey(), header.getValue());
      }
    }

    // If the content type was already set, do nothing.
    if (response.getContentType() == null) {
      // By default we use the content type of the transport.
      if (null == contentType) {
        response.setContentType(transport.contentType());
      } else {
        response.setContentType(contentType);
      }
    }

    // Send redirect
    if (null != redirectUri) {
      response.sendRedirect(redirectUri);
      response.setStatus(status); // HACK to override whatever status the redirect sets.
      return;
    }

    // Write out data.
    response.setStatus(status);

    if (null != templateKey) {
      response.getWriter().write(injector.getInstance(Templates.class).render(templateKey, entity));
    } else if (null != entity) {
      if (entity instanceof InputStream) {
        // Stream the response rather than marshalling it through a transport.
        InputStream inputStream = (InputStream) entity;
        try {
          IOUtils.copy(inputStream, response.getOutputStream());
        } finally {
          inputStream.close();
        }
      } else {
        // TODO(dhanji): This feels wrong to me. We need a better way to obtain the entity type.
        transport.out(response.getOutputStream(), (Class<E>) entity.getClass(), entity);
      }
    }
  }
}

