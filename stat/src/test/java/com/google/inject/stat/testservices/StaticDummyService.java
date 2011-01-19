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

/**
 * This class is a simple test-based service that has static members that are
 * tested for {@link Stat}-based publishing.
 *
 * @author ffaber@gmail.com (Fred Faber)
 */
public class StaticDummyService {
  public static final String STATIC_CALLS = "static-calls";

  @Stat(STATIC_CALLS)
  private static final AtomicInteger calls = new AtomicInteger();

  public static int getNumberOfStaticCalls() {
    return calls.intValue();
  }

  public void call() {
    calls.incrementAndGet();
  }
}
