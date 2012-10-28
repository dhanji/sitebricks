package com.google.sitebricks.channel;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.sitebricks.client.Transport;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class ObserverWrapper {
  static final String DEFAULT = "__$_sb:def_";
  private final Class<?> type;

  private Injector injector;
  private final Map<String, MethodDispatcher> receivers;

  public ObserverWrapper(String event, Class<?> type, Binder binder) {
    this.type = type;
    this.receivers = new HashMap<String, MethodDispatcher>();

    for (Method method : type.getDeclaredMethods()) {
      Observe observe = method.getAnnotation(Observe.class);
      if (observe != null) {
        if (method.getParameterTypes().length != 1)
          binder.addError("@Observe method must take exactly one argument: " + method);

        if (method.getReturnType() != void.class)
          binder.addError("@Observe method must not return any values: " + method);

        receivers.put(event, new MethodDispatcher(observe, method));
      }
    }
  }

  public void dispatch(String event, String data) {
    if (event == null)
      event = DEFAULT;

    MethodDispatcher method = receivers.get(event);
    Object instance = injector.getInstance(type);
    Transport transport = injector.getInstance(method.observe.value());

    try {
      Object in = transport.in(new ByteArrayInputStream(data.getBytes()),
          method.method.getParameterTypes()[0]);

      method.method.invoke(instance, in);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Dispatch error in @Observe method: " + method, e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException("Dispatch error in @Observe method: " + method, e);
    } catch (IOException e) {
      throw new RuntimeException("Dispatch error in @Observe method: " + method, e);
    }
  }

  @Inject
  void init(Injector injector) {
    this.injector = injector;
  }

  private static class MethodDispatcher {
    private final Observe observe;
    private final Method method;

    private MethodDispatcher(Observe observe, Method method) {
      this.observe = observe;
      this.method = method;
    }
  }
}
