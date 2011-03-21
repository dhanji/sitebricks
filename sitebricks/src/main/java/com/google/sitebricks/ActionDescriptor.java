package com.google.sitebricks;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Key;
import com.google.sitebricks.routing.Action;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Describes an action binding in the SPI for actions.
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class ActionDescriptor implements PageBinder.ActionBinder {
  private final Action action;
  private final Key<? extends Action> actionKey;

  private final Map<String, String> selectParams = Maps.newHashMap();
  private final Map<String, String> selectHeaders = Maps.newHashMap();

  private final Set<Class<? extends Annotation>> methods = Sets.newHashSet();

  // Pass thru for builder config.
  private final PageBinder.PerformBinder performBinder;

  public ActionDescriptor(Action action, PageBinder.PerformBinder performBinder) {
    this.action = action;
    this.performBinder = performBinder;
    this.actionKey = null;
  }

  public ActionDescriptor(Key<? extends Action> action, PageBinder.PerformBinder performBinder) {
    this.performBinder = performBinder;
    this.action = null;
    this.actionKey = action;
  }

  @Override
  public PageBinder.PerformBinder on(Class<? extends Annotation>... method) {
    Preconditions.checkArgument(null != method && method.length > 0,
        "Must specify at least one method");
    methods.addAll(Sets.newHashSet(method));
    return performBinder;
  }

  @Override
  public PageBinder.ActionBinder select(String param, String value) {
    Preconditions.checkArgument(param != null && !param.isEmpty(),
        "Parameter to select() must be a non-empty string");
    Preconditions.checkArgument(value != null && !value.isEmpty(),
        "Value to select() must be a non-empty string");
    selectParams.put(param, value);
    return this;
  }

  @Override
  public PageBinder.ActionBinder selectHeader(String header, String value) {
    Preconditions.checkArgument(header != null && !header.isEmpty(),
        "Header to selectHeader() must be a non-empty string");
    Preconditions.checkArgument(value != null && !value.isEmpty(),
        "Value to selectHeader() must be a non-empty string");
    selectHeaders.put(header, value);
    return this;
  }

  @Override
  public PageBinder.ActionBinder selectHeader(String param, Pattern regex) {
    throw new UnsupportedOperationException("To be implemented");
  }

  public Action getAction() {
    return action;
  }

  public Key<? extends Action> getActionKey() {
    return actionKey;
  }

  public Map<String, String> getSelectParams() {
    return selectParams;
  }

  public Map<String, String> getSelectHeaders() {
    return selectHeaders;
  }

  public Set<Class<? extends Annotation>> getMethods() {
    return methods;
  }
}
