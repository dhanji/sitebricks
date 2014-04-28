package org.sitebricks.web;

import com.google.inject.AbstractModule;
import com.google.sitebricks.At;
import com.google.sitebricks.Classes;

import java.util.Set;

import static com.google.inject.matcher.Matchers.annotatedWith;

/**
 * @author dhanji (Dhanji R. Prasanna)
 */
public class WebModule extends AbstractModule {
  private final Package pack;

  public WebModule(Package pack) {
    this.pack = pack;
  }

  @Override
  protected void configure() {
    Set<Class<?>> classes = Classes.matching(
        annotatedWith(At.class)
    ).in(pack);


  }
}
