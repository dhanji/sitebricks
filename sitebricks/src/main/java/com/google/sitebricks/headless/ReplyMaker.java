package com.google.sitebricks.headless;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.sitebricks.client.Transport;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.client.transport.Text;
import com.google.sitebricks.rendering.Strings;
import com.google.sitebricks.rendering.Templates;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.CharsetUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.LOCATION;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * A builder implementation of the Reply interface.
 */
public class ReplyMaker<E> extends Reply<E> {

  // By default, we cool.
  int status = HttpServletResponse.SC_OK;

  String contentType;

  String redirectUri;
  Map<String, String> headers = Maps.newHashMap();

  Key<? extends Transport> transport = Key.get(Json.class);
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
  public Reply<E> badRequest() {
    status = HttpServletResponse.SC_BAD_REQUEST;
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

  public ChannelFuture populate(Injector injector, ChannelHandlerContext context) throws IOException {
    // Write out data.
    ByteBuf buffer = Unpooled.copiedBuffer(new byte[0]);
    InputStream inputStream = null;
    if (null != templateKey) {
      buffer = Unpooled.copiedBuffer(injector.getInstance(Templates.class)
          .render(templateKey, entity), CharsetUtil.UTF_8);
    } else if (null != entity) {
      if (entity instanceof InputStream) {
        // Stream the response rather than marshalling it through a transport.
        inputStream = (InputStream) entity;
      } else {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        injector.getInstance(transport).out(bos, (Class<E>) entity.getClass(), entity);
        bos.write("\r\n".getBytes());
        buffer = Unpooled.copiedBuffer(bos.toByteArray());
      }
    }


    FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
        new HttpResponseStatus(status, ""),
        buffer);

    // This is where we take all the builder values and encode them in the response.
    Transport transport = injector.getInstance(this.transport);

    // Set any headers (we do this first, so we can override any cheekily set headers).
    if (!headers.isEmpty()) {
      for (Map.Entry<String, String> header : headers.entrySet()) {
        response.headers().set(header.getKey(), header.getValue());
      }
    }

    // If the content type was already set, do nothing.
    // By default we use the content type of the transport.
    if (null == contentType) {
      response.headers().set(CONTENT_TYPE, transport.contentType());
    } else {
      response.headers().set(CONTENT_TYPE, contentType);
    }

    // Send redirect
    if (null != redirectUri) {
      response.headers().set(LOCATION, redirectUri);
    }

    ChannelFuture future = context.write(response);
    if (inputStream != null) {
      final InputStream copied = inputStream;
      future.addListener(f -> context.write(new ChunkedStream(copied)));
    }

    return future;
  }

  @Override
  public boolean equals(Object other) {
	  if(!(other instanceof ReplyMaker<?>))
		  return false;
	  
	  @SuppressWarnings("unchecked")
	  ReplyMaker<E> o = (ReplyMaker<E>)other;
	  if(this.status != o.status)
		  return false;
	  
	  if((this.contentType != o.contentType)
	  && (this.contentType != null && !this.contentType.equals(o.contentType))
	  && (this.contentType == null && o.contentType != null))
		  return false;
	  
	  if((this.redirectUri != o.redirectUri)
	  && (this.redirectUri != null && !this.redirectUri.equals(o.redirectUri))
	  && (this.redirectUri == null && o.redirectUri != null))
		  return false;
	  	  
	  if(!this.headers.equals(o.headers))
		  return false;
	  
	  if(!this.transport.equals(o.transport))
		  return false;
	  
	  if(this.templateKey != o.templateKey)
		  return false;

	  if((this.entity != o.entity)
	  && (this.entity != null && !this.entity.equals(o.entity))
	  && (this.entity == null && o.entity != null))
		  return false;
	  
	  // All tests passed, the objects must be equal
	  return true;
  }
}

