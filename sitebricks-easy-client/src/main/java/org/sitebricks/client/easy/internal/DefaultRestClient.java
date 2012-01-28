/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sitebricks.client.easy.internal;

import com.google.sitebricks.At;
import com.google.sitebricks.client.Web;
import com.google.sitebricks.client.WebClient;
import com.google.sitebricks.client.WebResponse;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.http.Delete;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.http.Put;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

public class DefaultRestClient implements InvocationHandler {

  private static final HttpMethod[] METHODS = new HttpMethod[] { new GetHttpMethod(), new PutHttpMethod(), new DeleteHttpMethod(), new PostHttpMethod() };

  private final URL baseUrl;

  private final Web web;

  private final Map<String, String> bindings;

  private final Class<?> serviceInterface;

  private final ObjectMapper mapper;

  private final TypeFactory typeFactory;

  public DefaultRestClient(final Web web, final ObjectMapper mapper, final Class<?> serviceInterface, final Map<String, String> bindings, final URL baseUrl) {
    this.mapper = mapper;
    this.serviceInterface = serviceInterface;
    this.baseUrl = baseUrl;
    this.web = web;
    this.bindings = bindings;
    typeFactory = TypeFactory.defaultInstance();
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
    final HttpMethod httpMethod = getHttpMethod(method);
    final String url = getUrl(method, args);
    final Class<? extends Object> transportType = getTransportType(method);

    final WebClient<? extends Object> webClient = web.clientOf(url).transports(transportType).over(Json.class);
    try {
      @SuppressWarnings("unchecked")
      final WebResponse restResponse = httpMethod.invoke((WebClient<Object>) webClient, args == null || args.length == 0 ? null : args[0]);
      checkStatus(restResponse, url, httpMethod);

      final Class<?> returnType = method.getReturnType();
      if (returnType != void.class) {
        final Type genericReturnType = method.getGenericReturnType();
        final String responseBody = restResponse.toString();

        final Object response = mapper.readValue(responseBody, typeFactory.constructType(genericReturnType, serviceInterface));

        return response;
      }
    } finally {
      webClient.close();
    }
    return null;
  }

  private String getUrl(final Method method, final Object[] args) {
    final StringBuilder rawUrl = new StringBuilder();
    final At atClass = serviceInterface.getAnnotation(At.class);
    if (atClass != null) {
      rawUrl.append(atClass.value());
    } else {
      final At atDeclaringClass = method.getDeclaringClass().getAnnotation(At.class);
      if (atDeclaringClass != null) {
        rawUrl.append(atDeclaringClass.value());
      }
    }
    final At atMethod = method.getAnnotation(At.class);
    if (atMethod != null) {
      rawUrl.append(atMethod.value());
    }
    if (rawUrl.toString().trim().length() == 0) {
      throw new IllegalStateException(String.format("Cannot calculate rest URL for [%s]. Is class and/or method annotated with @At?", method.getName()));
    }

    String url = rawUrl.toString();
    if (url.contains(":")) {
      final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
      for (int i = 0; i < parameterAnnotations.length; i++) {
        if (args.length >= i) {
          final Annotation[] annotations = parameterAnnotations[i];
          for (final Annotation annotation : annotations) {
            if (annotation instanceof javax.inject.Named) {
              final String name = ((javax.inject.Named) annotation).value();
              if (args[i] != null) {
                url = url.replace(":" + name, args[i].toString());
              }
            }
            if (annotation instanceof com.google.inject.name.Named) {
              final String name = ((com.google.inject.name.Named) annotation).value();
              if (args[i] != null) {
                url = url.replace(":" + name, args[i].toString());
              }
            }
          }
        }
      }
      if (url.contains(":")) {
        for (final Map.Entry<String, String> binding : bindings.entrySet()) {
          url = url.replace(":" + binding.getKey(), binding.getValue());
        }
      }
      if (url.contains(":")) {
        throw new IllegalStateException(String.format("Cannot calculate rest URL for [%s] as not all bindings could be resolved [%s]. "
            + "Are all paramters matching the missing bindings annotated with @Named?", method.getName(), url));
      }
    }
    try {
      return new URI(baseUrl + url).normalize().toASCIIString();
    } catch (final URISyntaxException e) {
      throw new IllegalStateException(String.format("Cannot calculate rest URL for [%s]. Is class and/or method annotated with @At?", method.getName()), e);
    }
  }

  private static HttpMethod getHttpMethod(final Method method) {
    for (final HttpMethod httpMethod : METHODS) {
      final Annotation annotation = method.getAnnotation(httpMethod.annotation());
      if (annotation != null) {
        return httpMethod;
      }
    }
    throw new IllegalStateException(String.format("Could not determine the http method to call. Is you method [%s] with one of @Get, @Put, @Delete, @Post?", method.getName()));
  }

  private static void checkStatus(final WebResponse response, final String url, final HttpMethod httpMethod) {
    final int status = response.status();
    if (status < 200 || status >= 300) {
      throw new IllegalStateException(String.format("Server returned status code [%s] while invoking [%s] on [%s]", status, httpMethod, url));
    }
  }

  private static Class<? extends Object> getTransportType(final Method method) {
    Class<? extends Object> transportType = Void.class;
    final Class<?>[] parameterTypes = method.getParameterTypes();
    if (parameterTypes.length > 0) {
      transportType = parameterTypes[0];
    }
    return transportType;
  }

  private static interface HttpMethod {
    Class<? extends Annotation> annotation();

    Class<? extends Object> getTransportType(final Method method);

    <T> WebResponse invoke(WebClient<T> webClient, T request);
  }

  private static class GetHttpMethod implements HttpMethod {

    @Override
    public Class<? extends Annotation> annotation() {
      return Get.class;
    }

    @Override
    public <T> WebResponse invoke(final WebClient<T> webClient, final T request) {
      return webClient.get();
    }

    @Override
    public Class<? extends Object> getTransportType(final Method method) {
      return Void.class;
    }

    @Override
    public String toString() {
      return "GET";
    }

  }

  private static class PutHttpMethod implements HttpMethod {

    @Override
    public Class<? extends Annotation> annotation() {
      return Put.class;
    }

    @Override
    public <T> WebResponse invoke(final WebClient<T> webClient, final T request) {
      return webClient.put(request);
    }

    @Override
    public Class<? extends Object> getTransportType(final Method method) {
      return DefaultRestClient.getTransportType(method);
    }

    @Override
    public String toString() {
      return "PUT";
    }

  }

  private static class DeleteHttpMethod implements HttpMethod {

    @Override
    public Class<? extends Annotation> annotation() {
      return Delete.class;
    }

    @Override
    public <T> WebResponse invoke(final WebClient<T> webClient, final T request) {
      return webClient.delete();
    }

    @Override
    public Class<? extends Object> getTransportType(final Method method) {
      return DefaultRestClient.getTransportType(method);
    }

    @Override
    public String toString() {
      return "DELETE";
    }

  }

  private static class PostHttpMethod implements HttpMethod {

    @Override
    public Class<? extends Annotation> annotation() {
      return Post.class;
    }

    @Override
    public <T> WebResponse invoke(final WebClient<T> webClient, final T request) {
      return webClient.post(request);
    }

    @Override
    public Class<? extends Object> getTransportType(final Method method) {
      return DefaultRestClient.getTransportType(method);
    }

    @Override
    public String toString() {
      return "POST";
    }

  }

}
