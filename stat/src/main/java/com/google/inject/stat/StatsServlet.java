package com.google.inject.stat;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
class StatsServlet extends HttpServlet {
  private final Stats stats;

  @Inject
  public StatsServlet(Stats stats) {
    this.stats = stats;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    Map<String,String> snapshot = stats.snapshot();

    // Render as HTML by default. JSON if asked for.
    String accept = req.getHeader("Accept");
    if (accept != null && (accept.contains("json") || accept.contains("JSON"))) {
      // Return as JSON.
      // ...
    } else {
      renderHtml(snapshot, resp);
    }
  }

  private void renderHtml(Map<String, String> snapshot, HttpServletResponse response) throws IOException {
    response.setContentType("text/html");

    PrintWriter writer = response.getWriter();
    writer.write("<html><head><style>body { font-family: monospace; }</style></head><body>");
    for (Map.Entry<String, String> entry : snapshot.entrySet()) {
      writer.write("<b>");
      writer.write(entry.getKey());
      writer.write(":</b> ");
      writer.write(entry.getValue());
      writer.write("<br/> ");
    }
    writer.write("</body></html>");

    writer.flush();
    writer.close();
  }
}
