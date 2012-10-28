package com.google.sitebricks.channel;

import com.google.common.collect.ImmutableSet;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Install to set up sitebricks channel support for websocket (or other available
 * transports, such as Google AppEngine's Channel API).
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public abstract class ChannelModule extends ServletModule {
  static final String CHANNEL_URL_NAME = "__SB:CHANNEL_URL";
  private final String channelUrl;
  private static final Set<String> SUPPORTED_SYSTEMS =
      ImmutableSet.of(
          "jetty-websocket",
          "jetty-continuation"
      );

  private final Map<String, ObserverWrapper> handlers = new HashMap<String, ObserverWrapper>();

  public ChannelModule(String channelUrl) {
    if (channelUrl == null || channelUrl.isEmpty()
        || !channelUrl.startsWith("/"))
      addError("ChannelModule URL must begin with '/' but found: " + channelUrl);
    else if (channelUrl.endsWith("/") && channelUrl.length() > 1)
        channelUrl = channelUrl.substring(0, channelUrl.length() - 1);

    this.channelUrl = channelUrl;
  }

  @Override
  protected final void configureServlets() {
    configureChannels();

    bind(Switchboard.class).to(ChannelSwitchboard.class);
    bind(new TypeLiteral<Map<String, ObserverWrapper>>() {})
        .annotatedWith(Handlers.class)
        .toInstance(handlers);

    bindConstant().annotatedWith(Names.named(CHANNEL_URL_NAME)).to(channelUrl);

    // Appengine?
    if (System.getProperty("com.google.appengine.runtime.version") != null) {
      serve(channelUrl).with(AppengineRoutingServlet.class);
    } else {
      // Test if Jetty-websocket is on classpath.
      boolean websocket = true;
      try {
        Class.forName("org.eclipse.jetty.websocket.WebSocketServlet");
        serve(channelUrl + "/websocket").with(WebSocketRoutingServlet.class);
      } catch (ClassNotFoundException cnfe) {
        websocket = false;
      }

      // Also try Jetty-continuations.
      try {
        Class.forName("org.eclipse.jetty.continuation.ContinuationSupport");
        serve(channelUrl + "/async").with(ContinuationRoutingServlet.class);
      } catch (ClassNotFoundException cnfe2) {
        // Give up?
        if (!websocket)
          addError("No available browser-socket subsystems were found on the classpath. " +
              "Currently supported are: " + SUPPORTED_SYSTEMS);
      }

      serve(channelUrl + CometJSServlet.SOCKET_URL_PATTERN).with(CometJSServlet.class);
      serve(channelUrl + CometJSServlet.JQUERY_URL_PATTERN).with(CometJSServlet.class);
    }
  }

  protected final ChannelObserverBinder process(final String event) {
    return new ChannelObserverBinder() {
      @Override
      public void with(Class<?> clazz) {
        handlers.put(event, new ObserverWrapper(event, clazz, binder()));
      }
    };
  }

  protected final ChannelObserverBinder processAll() {
    return new ChannelObserverBinder() {
      @Override
      public void with(Class<?> clazz) {
        ObserverWrapper observer = new ObserverWrapper(ObserverWrapper.DEFAULT,
            clazz, binder());
        requestInjection(observer);
        handlers.put(ObserverWrapper.DEFAULT, observer);
      }
    };
  }

  protected abstract void configureChannels();

  public static interface ChannelObserverBinder {
    void with(Class<?> clazz);
  }
}
