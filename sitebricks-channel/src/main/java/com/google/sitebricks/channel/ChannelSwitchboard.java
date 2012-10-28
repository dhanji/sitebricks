package com.google.sitebricks.channel;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
class ChannelSwitchboard implements Switchboard {
  private final Map<String, ObserverWrapper> handlers;
  private final ConcurrentMap<String, Channel> channels =
      new ConcurrentHashMap<String, Channel>();

  @Inject
  ChannelSwitchboard(@Handlers Map<String, ObserverWrapper> handlers) {
    this.handlers = handlers;
  }

  public void receive(String data) {
    String[] split = data.split(":", 1);
    if (split.length > 1)
      handlers.get(split[0]).dispatch(split[0], split[1]);
    else
      handlers.get(ObserverWrapper.DEFAULT).dispatch(null, split[0]);
  }

  @Override
  public Channel named(String name) {
    return channels.get(name);
  }

  public void connect(String socketId, Channel channel) {
    channels.put(socketId, channel);
  }

  public void disconnect(String socketId) {
    channels.remove(socketId);
  }
}
