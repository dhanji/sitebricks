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
import com.google.common.collect.Maps;
import com.google.gson.Gson;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class contains a collection of {@link StatsPublisher} implementations.
 *
 * @author ffaber@gmail.com (Fred Faber)
 */
final class StatsPublishers {
  private StatsPublishers() { }

  /** This {@link StatsPublisher} publishes snapshots as html. */
  static class HtmlStatsPublisher extends StatsPublisher {

    @Override protected String getContentType() {
      return "text/html";
    }

    @Override protected void publish(
        ImmutableMap<StatDescriptor, Object> snapshot, PrintWriter writer) {
      writer.println("<html><head><style>");
      writer.println("body { font-family: monospace; }");
      writer.println("</style></head><body>");
      for (Entry<StatDescriptor, Object> entry : snapshot.entrySet()) {
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

  /** This {@link StatsPublisher} publishes a snapshot as JSON. */
  static class JsonStatsPublisher extends StatsPublisher {

    @Override protected String getContentType() {
      return "application/json";
    }

    @Override protected void publish(
        ImmutableMap<StatDescriptor, Object> snapshot, PrintWriter writer) {
      Gson gson = new Gson();
      Map<String, Object> valuesByName = Maps.newLinkedHashMap();
      for (Entry<StatDescriptor, Object> entry : snapshot.entrySet()) {
        valuesByName.put(entry.getKey().getName(), entry.getValue());
      }

      String gsonString = gson.toJson(valuesByName);
      writer.write(gsonString);
    }
  }

  /** This {@link StatsPublisher} publishes snapshots as text. */
  static class TextStatsPublisher extends StatsPublisher {

    @Override protected String getContentType() {
      return "text/plain";
    }

    @Override protected void publish(
        ImmutableMap<StatDescriptor, Object> snapshot, PrintWriter writer) {
      for (Map.Entry<StatDescriptor, Object> entry : snapshot.entrySet()) {
        writer.println(entry.getKey().getName() + " " + entry.getValue());
      }
    }
  }
}
