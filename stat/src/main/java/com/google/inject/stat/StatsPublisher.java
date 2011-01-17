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

/**
 * An implementation of a {@link StatsPublisher} is able to publish a snapshot
 * of values, as annotated by {@link Stat}.
 *
 * @author ffaber@gmail.com (Fred Faber)
 */
public abstract class StatsPublisher {
  /**
   * Returns a string indicating the content type. The string should be a
   * suitable value for setting the content type of a servlet request.
   */
  protected abstract String getContentType();

  /**
   * Publishes the given {@code snapshot} to the given {@link PrintWriter},
   * where the values of the snapshot are the values of the stats by which
   * each is keyed.
   */
  protected abstract void publish(
      ImmutableMap<StatDescriptor, Object> snapshot, PrintWriter writer);
}
