package com.google.sitebricks.client;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import net.jcip.annotations.Immutable;

import java.util.Map;

/**
 * JdkWeb is a general Web application which provides web functionality that uses
 * the Java URL api that comes with the JDK.
 *
 * @author Miroslav Genov (mgenov@gmail.com)
 */
@Immutable
public class JdkWeb implements Web {

  private final Provider<WebClientBuilder> builder;
  private final Injector injector;
  private String url;
  private Map<String, String> headers;

  @Inject
  public JdkWeb(Provider<WebClientBuilder> builder, Injector injector) {
    this.builder = builder;
    this.injector = injector;
  }

  public FormatBuilder clientOf(String url) {
    this.url = url;
    return new ForwardingFormatBuilder(builder.get()) {
      @Override
      public <T> ReadAsBuilder<T> transports(Class<T> clazz) {
        return new InternalReadAsBuilder<T>(clazz);
      }
    };
  }

  public FormatBuilder clientOf(String url, Map<String, String> headers) {
    this.url = url;
    this.headers = headers;
    return new ForwardingFormatBuilder(builder.get()) {
      @Override
      public <T> ReadAsBuilder<T> transports(Class<T> clazz) {
        return new InternalReadAsBuilder<T>(clazz);
      }
    };
  }

  private class InternalReadAsBuilder<T> implements Web.ReadAsBuilder<T> {
    private final Class<T> transporting;

    private InternalReadAsBuilder(Class<T> transporting) {
      this.transporting = transporting;
    }

    public WebClient<T> over(Class<? extends Transport> transport) {
      return new JdkWebClient<T>(injector, url,headers, transporting, Key.get(transport));
    }
  }
}
