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
 * This implementation of {@link StatsPublisher} publishes a snapshot as JSON.
 *
 * @author ffaber@gmail.com (Fred Faber)
 */
class JsonStatsPublisher extends StatsPublisher {

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
