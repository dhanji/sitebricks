package com.google.sitebricks.slf4j;

import com.google.inject.AbstractModule;

import static com.google.inject.matcher.Matchers.any;

/**
 * Module to install which enables automatic injection of slf4j loggers into
 * Guice-managed objects (by field injection only).
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Slf4jModule extends AbstractModule {
  @Override
  protected void configure() {
    bindListener(any(), new Slf4jInjectionTypeListener());
  }
}
