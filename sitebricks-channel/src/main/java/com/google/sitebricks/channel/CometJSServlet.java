package com.google.sitebricks.channel;

import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
class CometJSServlet extends HttpServlet {
  static final String JQUERY_URL_PATTERN = "/jquery-1.7.1.js";
  static final String SOCKET_URL_PATTERN = "/__js";
  private static final String SOCKET_JS;
  private final AtomicReference<String> jqueryJs = new AtomicReference<String>();

  static {
    try {
      SOCKET_JS = IOUtils.toString(CometJSServlet.class.getResourceAsStream("socket.js"), "UTF-8");
    } catch (IOException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private final String channelUrl;

  @Inject
  CometJSServlet(@Named(ChannelModule.CHANNEL_URL_NAME) String channelUrl) {
    this.channelUrl = channelUrl;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    resp.setContentType("application/javascript");
    if (req.getRequestURI().endsWith(JQUERY_URL_PATTERN)) {
      String jqueryJs = this.jqueryJs.get();
      if (jqueryJs == null) {
        jqueryJs = IOUtils.toString(CometJSServlet.class.getResourceAsStream("jquery-1.7.1.min.js"), "UTF-8");
        this.jqueryJs.compareAndSet(null, jqueryJs);
      }

      resp.setHeader("Cache-Control", "max-age=315360000");
      IOUtils.write(jqueryJs, resp.getOutputStream());
      return;
    }

    resp.setHeader("Pragma", "no-cache"); // HTTP 1.0
    resp.setHeader("Expires", "Thu, 01 Dec 1994 16:00:00 GMT"); // HTTP 1.0
    resp.setHeader("Cache-Control", "no-cache"); // HTTP 1.1
    IOUtils.write(SOCKET_JS
        .replaceFirst("\\$_SITEBRICKS_URL_PREFIX_\\$", channelUrl)
        .replaceFirst("\\$_SITEBRICKS_SOCKET_ID_\\$", UUID.randomUUID().toString()),
        resp.getOutputStream());
  }
}
