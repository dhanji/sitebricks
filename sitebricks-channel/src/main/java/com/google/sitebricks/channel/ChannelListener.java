package com.google.sitebricks.channel;

import com.google.inject.ImplementedBy;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@ImplementedBy(SinkingChannelListener.class)
public interface ChannelListener {
  void connected(Switchboard.Channel channel);

  void disconnected(Switchboard.Channel channel);
}
