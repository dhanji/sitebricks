package com.google.inject.stat;

import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.getStackTraceAsString;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
class StatsServlet extends HttpServlet {
  static final String FORMAT_PARAM = "format";
  static final String DEFAULT_FORMAT = "default";

  private final Stats stats;
  private final Map<String, StatsPublisher> publishersByFormat;

  @Inject
  StatsServlet(Map<String, StatsPublisher> publishersByFormat, Stats stats) {
    this.publishersByFormat = publishersByFormat;
    this.stats = stats;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setStatus(HttpServletResponse.SC_OK);
    String format = firstNonNull(req.getParameter(FORMAT_PARAM), DEFAULT_FORMAT);
    StatsPublisher publisher = checkNotNull(publishersByFormat.get(format),
        "No publisher for format %s found in %s",
        format, publishersByFormat);

    resp.setContentType(publisher.getContentType());

    PrintWriter writer = resp.getWriter();
    try {
      publisher.publish(stats.snapshot(), writer);
    } catch (Exception e) {
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      writer.write(String.format(
          "Exception publishing stats:\n%s", getStackTraceAsString(e)));
    } finally {
      writer.flush();
      writer.close();
    }
  }
}
