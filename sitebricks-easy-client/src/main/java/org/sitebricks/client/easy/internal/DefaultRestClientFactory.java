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

import com.google.sitebricks.client.Web;
import com.google.sitebricks.client.transport.JacksonJsonTransport;

import org.sitebricks.client.easy.RestClientFactory;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import static java.lang.reflect.Proxy.newProxyInstance;

class DefaultRestClientFactory<S> implements RestClientFactory<S> {

  @Inject
  private Web web;

  @Inject
  private JacksonJsonTransport transport;

  private final Class<S> serviceInterface;

  private final Map<String, String> bindings;

  DefaultRestClientFactory(final Class<S> serviceInterface, final Map<String, String> bindings) {
    this.serviceInterface = serviceInterface;
    this.bindings = bindings;
  }

  @Override
  public S create(final String baseUrl) {
    try {
      return create(new URL(baseUrl));
    } catch (final MalformedURLException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public S create(final URL baseUrl) {
    final Class<?>[] interfaces = new Class<?>[] { serviceInterface };
    return (S) newProxyInstance(serviceInterface.getClassLoader(), interfaces, new DefaultRestClient(web, transport.getObjectMapper(), serviceInterface, bindings, baseUrl));
  }

  @Override
  public S create(URI baseUrl) {
    try {
      return create(baseUrl.toURL());
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
