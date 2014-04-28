package com.google.sitebricks.channel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import com.google.sitebricks.client.transport.Json;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 * @author Jason van Zyl
 */
@Singleton
class WebSocketRoutingServlet extends WebSocketServlet {
  private final ChannelSwitchboard switchboard;
  private final Provider<ChannelListener> channelListener;
  private final Json json;

  @Inject
  WebSocketRoutingServlet(ChannelSwitchboard switchboard, Provider<ChannelListener> channelListener, Json json) {
    this.switchboard = switchboard;
    this.channelListener = channelListener;
    this.json = json;
  }

  @Override
  public void configure(WebSocketServletFactory factory) {
    factory.setCreator(new SitebricksWebSocketCreator());
  }

  public class SitebricksWebSocketCreator implements WebSocketCreator {    
    @Override
    public Object createWebSocket(UpgradeRequest req, UpgradeResponse resp) {
      String socketId = req.getParameterMap().get(Switchboard.SB_SOCKET_ID)[0];
      return new ChannelSocket(socketId);
    }    
  }
  
  @WebSocket
  public class ChannelSocket implements Switchboard.Channel {
    
    private String socketId;
    private Session session;
    
    public ChannelSocket(String socketId) {
      this.socketId = socketId;
    }
        
    @OnWebSocketConnect
    public void onWebSocketConnect(Session session) {
      this.session = session;
      switchboard.connect(socketId, this);
      channelListener.get().connected(this);
    }
    
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
    }

    @OnWebSocketMessage 
    public void onWebSocketText(String message) {
      switchboard.receive(message);
    }

    @OnWebSocketError
    public void onWebSocketError(Throwable cause) {
    }

    @OnWebSocketClose
    public void onWebSocketClose(int statusCode, String reason) {
      try {
        channelListener.get().disconnected(this);
      } finally {
        switchboard.disconnect(socketId);
      }
    }

    //
    // Switchboard.Channel
    //
    
    @Override
    public <E> void send(E reply) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try {
        json.out(out, null, reply);
        byte[] bytes = out.toByteArray();
        session.getRemote().sendString(new String(bytes));

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }    
    
    @Override
    public String getName() {
      return socketId;
    }
  }
}
