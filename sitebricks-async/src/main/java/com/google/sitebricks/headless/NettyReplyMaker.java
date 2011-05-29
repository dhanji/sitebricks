package com.google.sitebricks.headless;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.sitebricks.client.Transport;
import com.google.sitebricks.rendering.Templates;
import org.apache.commons.io.IOUtils;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

/**
 * Sits here so it is able to access the internals of ReplyMaker.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
public class NettyReplyMaker {
  private final Injector injector;

  @Inject
  public NettyReplyMaker(Injector injector) {
    this.injector = injector;
  }

  public <E> HttpResponse populate(Reply<E> o) throws IOException {
    // Should be enforced by the Sitebricks page validator.
    assert o instanceof ReplyMaker;

    ReplyMaker<E> reply = (ReplyMaker<E>) o;
    DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
        HttpResponseStatus.valueOf(reply.status));

    Transport transport = injector.getInstance(reply.transport);

    if (!reply.headers.isEmpty()) {
      for (Entry<String, String> entry : reply.headers.entrySet()) {
        response.setHeader(entry.getKey(), entry.getValue());
      }
    }

    // By default we use the content type of the transport.
    if (null == reply.contentType) {
      response.setHeader(Names.CONTENT_TYPE, transport.contentType());
    } else {
      response.setHeader(Names.CONTENT_TYPE, reply.contentType);
    }

    if (null != reply.redirectUri) {
      response.setHeader(Names.LOCATION, reply.redirectUri);
      return response;
    }

    // Write the entity data to our output buffer.
    if (null != reply.templateKey) {
      String output = injector.getInstance(Templates.class).render(reply.templateKey,
          reply.entity);
      response.setContent(ChannelBuffers.copiedBuffer(output, CharsetUtil.UTF_8));
    } else if (null != reply.entity) {
      if (reply.entity instanceof InputStream) {
        // Buffer the stream and write it, rather than marshalling it through a transport.
        // Note this exists for compatibility with the sync model, and can be quite expensive.
        InputStream inputStream = (InputStream) reply.entity;
        try {
          byte[] bytes = IOUtils.toByteArray(inputStream);
          response.setContent(ChannelBuffers.copiedBuffer(bytes));
        } finally {
          inputStream.close();
        }
      } else {
        // TODO(dhanji): This feels wrong to me. We need a better way to obtain the entity type.
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        transport.out(out, (Class<E>) reply.entity.getClass(), reply.entity);
        response.setContent(ChannelBuffers.copiedBuffer(out.toByteArray()));
      }
    }

    return response;
  }
}
