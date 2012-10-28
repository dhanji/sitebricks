package com.google.sitebricks.example;

import com.google.sitebricks.At;
import com.google.sitebricks.Show;
import com.google.sitebricks.channel.ChannelListener;
import com.google.sitebricks.channel.Observe;
import com.google.sitebricks.channel.Switchboard;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/chat") @Show("Chatter.html") @Singleton
public class Chatter {
  private final List<String> connected = new CopyOnWriteArrayList<String>();

  @Inject
  private Switchboard switchboard;

  @Observe
  public void receiveChat(String msg) {
    System.out.println("Chat msg received: " + msg);

    for (String socketId : connected) {
      switchboard.named(socketId).send("rebound from server: " + msg + " =)");
    }
  }

  @Singleton
  public static class ChatterListener implements ChannelListener {
    @Inject
    private Chatter chatter;

    @Override
    public void connected(Switchboard.Channel channel) {
      System.out.println("Channel connected with name: " + channel.getName());
      chatter.connected.add(channel.getName());
    }

    @Override
    public void disconnected(Switchboard.Channel channel) {
      System.out.println("Channel disconnected with name: " + channel.getName());
      chatter.connected.remove(channel.getName());
    }
  }
}
