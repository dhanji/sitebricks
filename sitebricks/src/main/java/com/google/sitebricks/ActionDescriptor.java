package com.google.sitebricks;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
  private final Class<? extends Action> actionClass;

  private final Map<String, String> selectParams = Maps.newHashMap();
  private final Map<String, String> selectHeaders = Maps.newHashMap();

  private final Set<Class<? extends Annotation>> methods = Sets.newHashSet();

  public ActionDescriptor(Action action) {
    this.action = action;
    this.actionClass = null;
  }

  @Override
  public PageBinder.ActionBinder on(Class<? extends Annotation>... method) {
    Preconditions.checkArgument(null != method && method.length > 0,
        "Must specify at least one method");
    methods.addAll(Sets.newHashSet(method));
    return this;
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
    selectParams.put(header, value);
    return this;
  }

  @Override
  public PageBinder.ActionBinder selectHeader(String param, Pattern regex) {
    throw new UnsupportedOperationException("To be implemented");
  }

  public Action getAction() {
    return action;
  }

  public Class<? extends Action> getActionClass() {
    return actionClass;
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
