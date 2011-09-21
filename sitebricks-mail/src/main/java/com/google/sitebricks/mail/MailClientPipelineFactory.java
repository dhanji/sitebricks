package com.google.sitebricks.mail;

import com.google.sitebricks.mail.Mail.Auth;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class MailClientPipelineFactory implements ChannelPipelineFactory {
  private final MailClientHandler mailClientHandler;
  private final MailClientConfig config;

  public MailClientPipelineFactory(MailClientHandler mailClientHandler, MailClientConfig config) {
    this.mailClientHandler = mailClientHandler;
    this.config = config;
  }

  public ChannelPipeline getPipeline() throws Exception {
    // Create a default pipeline implementation.
    ChannelPipeline pipeline = Channels.pipeline();

    if (config.getAuthType() == Auth.SSL) {
      SSLEngine sslEngine = SSLContext.getDefault().createSSLEngine();
      sslEngine.setUseClientMode(true);
      SslHandler sslHandler = new SslHandler(sslEngine);
      sslHandler.setEnableRenegotiation(true);
      pipeline.addLast("ssl", sslHandler);
    }

    // Add the text line codec combination first,
    pipeline.addLast("framer", new DelimiterBasedFrameDecoder(12192, Delimiters.lineDelimiter()));
    pipeline.addLast("decoder", new StringDecoder());
    pipeline.addLast("encoder", new StringEncoder());

    // and then business logic.
    pipeline.addLast("handler", mailClientHandler);

    return pipeline;
  }
}
