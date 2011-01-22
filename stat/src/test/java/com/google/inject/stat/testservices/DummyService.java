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

package com.google.inject.stat.testservices;

import com.google.inject.stat.Stat;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This is a simple dummy class that is used in tests to ensure that a private
 * field on a class outside of a package can have its {@link Stat} fields read
 * and published.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class DummyService {
  public static final String NUMBER_OF_CALLS = "number-of-calls";

  @Stat(NUMBER_OF_CALLS)
  private final AtomicInteger calls = new AtomicInteger();

  public static final String CALL_LATENCY_NS = "call-latency-ns";

  private final AtomicLong callLatencyNs = new AtomicLong(0L);

  /**
   * Increments {@link #calls} by one.  Also increments {@link #callLatencyNs}
   * by the amount of time required to do so..
   */
  public void call() {
    Long startTime = System.nanoTime();
    try {
      calls.incrementAndGet();
    } finally {
      callLatencyNs.addAndGet(System.nanoTime() - startTime);
    }
  }

  public AtomicInteger getCalls() {
    return calls;
  }

  @Stat(CALL_LATENCY_NS)
  public Long getCallLatencyMs() {
    return callLatencyNs.get();
  }
}
