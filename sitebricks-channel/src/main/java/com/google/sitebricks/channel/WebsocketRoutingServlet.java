package com.google.sitebricks.channel;

import com.google.sitebricks.client.transport.Json;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
class WebSocketRoutingServlet extends WebSocketServlet {
  private final ChannelSwitchboard switchboard;
  private final Provider<ChannelListener> channelListener;
  private final Json json;

  @Inject
  WebSocketRoutingServlet(ChannelSwitchboard switchboard,
                          Provider<ChannelListener> channelListener,
                          Json json) {
    this.switchboard = switchboard;
    this.channelListener = channelListener;
    this.json = json;
  }

  @Override
  public WebSocket doWebSocketConnect(HttpServletRequest request, String s) {
    final String socketId = request.getParameter(Switchboard.SB_SOCKET_ID);
    if (socketId == null)
      throw new IllegalStateException("Invalid websocket upgrade request--must contain an" +
          " identifying parameter: " + Switchboard.SB_SOCKET_ID);

    return new WebSocket.OnTextMessage() {
      private WebSocketChannel channel;

      @Override
      public void onMessage(String message) {
        switchboard.receive(message);
      }

      @Override
      public void onOpen(Connection connection) {
        this.channel = new WebSocketChannel(socketId, connection);

        switchboard.connect(socketId, channel);
        channelListener.get().connected(channel);
      }

      @Override
      public void onClose(int i, String s) {
        try {
          channelListener.get().disconnected(this.channel);
        } finally {
          switchboard.disconnect(socketId);
        }
      }
    };
  }

  private class WebSocketChannel implements Switchboard.Channel {
    private final WebSocket.Connection connection;
    private final String socketId;

    private WebSocketChannel(String socketId, WebSocket.Connection connection) {
      this.socketId = socketId;
      this.connection = connection;
    }

    @Override
    public String getName() {
      return socketId;
    }

    @Override
    public <E> void send(E reply) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try {
        json.out(out, null, reply);
        byte[] bytes = out.toByteArray();
        connection.sendMessage(new String(bytes));

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
