/**
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.google.inject.stat;

import com.google.common.collect.ImmutableMap;

import java.io.PrintWriter;
import java.util.Map;

/**
 * This implementation of {@link StatsPublisher} publishes snapshots as html.
 *
 * @author ffaber@gmail.com (Fred Faber)
 */
class HtmlStatsPublisher extends StatsPublisher {

  @Override String getContentType() {
    return "text/html";
  }

  @Override void publish(
      ImmutableMap<StatDescriptor, Object> snapshot, PrintWriter writer) {
    writer.println("<html><head><style>");
    writer.println("body { font-family: monospace; }");
    writer.println("</style></head><body>");
    for (Map.Entry<StatDescriptor, Object> entry : snapshot.entrySet()) {
      StatDescriptor statDescriptor = entry.getKey();
      writer.print("<b>");
      writer.print(statDescriptor.getName());
      writer.print(":</b> ");
      writer.print(entry.getValue());
      writer.println("<br/>");
    }
    writer.println("</body></html>");
  }
}
