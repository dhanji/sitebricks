package org.sitebricks.demo;

import com.google.sitebricks.At;
import com.google.sitebricks.channel.ChannelListener;
import com.google.sitebricks.channel.Observe;
import com.google.sitebricks.channel.Switchboard;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;

/**
 * @author dhanji (Dhanji R. Prasanna)
 */
@At("/hello")
public class Hello implements ChannelListener {
  @Get
  Reply hi(Request request) {
    return Reply.with("hello");
  }

  @Observe
  void socketHi(String message) {
    System.out.println(message);
  }

  @Override
  public void connected(Switchboard.Channel channel) {
    channel.send("{1:2}");
  }

  @Override
  public void disconnected(Switchboard.Channel channel) {

  }
}
