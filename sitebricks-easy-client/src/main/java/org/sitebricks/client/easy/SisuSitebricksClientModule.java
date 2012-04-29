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
package org.sitebricks.client.easy;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.util.Types;

import org.sitebricks.client.easy.internal.RestClientFactoryProvider;

import java.util.HashMap;
import java.util.Map;

public class SisuSitebricksClientModule {

  private final Map<String, String> bindings;

  public SisuSitebricksClientModule() {
    bindings = new HashMap<String, String>();
  }

  public <S> Module build(final Class<S> serviceInterface) {
    return new AbstractModule() {
      @Override
      protected void configure() {
        final Provider<RestClientFactory<S>> provider = new RestClientFactoryProvider<S>(serviceInterface, bindings);
        bind((Key) Key.get(Types.newParameterizedType(RestClientFactory.class, serviceInterface))).toProvider(provider);
      }
    };
  }

  public static SisuSitebricksClientModule restClientModule() {
    return new SisuSitebricksClientModule();
  }

  public VariablesBindingBuilder bind(final String name) {
    return new VariablesBindingBuilder(name);
  }

  public class VariablesBindingBuilder {
    private final String name;

    private VariablesBindingBuilder(final String name) {
      if (name == null) {
        throw new IllegalArgumentException("Cannot bind a null variable");
      }
      this.name = name;
    }

    public SisuSitebricksClientModule to(final Object value) {
      if (value == null) {
        throw new IllegalArgumentException(String.format("Value binded to [%s] variable cannot be null", name));
      }
      bindings.put(name, value.toString());
      return SisuSitebricksClientModule.this;
    }
  }

}
