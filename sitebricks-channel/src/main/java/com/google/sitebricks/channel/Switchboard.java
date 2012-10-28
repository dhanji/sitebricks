package com.google.sitebricks.channel;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public interface Switchboard {
  public static final String SB_SOCKET_ID = "SBSocketId";

  Channel named(String name);

  public static interface Channel {
    String getName();

    <E> void send(E reply);
  }
}
