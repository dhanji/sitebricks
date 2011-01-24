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

import com.google.common.collect.Lists;
import com.google.inject.stat.Stat;
import com.google.inject.stat.StatExposers.IdentityExposer;
import com.google.inject.stat.StatExposers.InferenceExposer;
import com.google.inject.stat.StatExposers.ToStringExposer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class has the main purpose of containing {@link Stat}-annotated members
 * so that it may be used to test {@code StatExposer}-based functionality.
 *
 * @author ffaber@gmail.com (Fred Faber)
 */
public class StatExposerTestingService {
  public static final String CALLS_WITH_DEFAULT_EXPOSER =
      "calls-with-default-exposer";
  public static final String CALLS_WITH_IDENTITY_EXPOSER =
      "calls-with-identity-exposer";
  public static final String CALLS_WITH_INFERENCE_EXPOSER =
      "calls-with-inference-exposer";
  public static final String CALLS_WITH_TO_STRING_EXPOSER =
      "calls-with-to-string-exposer";

  @Stat(CALLS_WITH_DEFAULT_EXPOSER)
  private final AtomicInteger callsWithDefaultExposer = new AtomicInteger();

  @Stat(value = CALLS_WITH_IDENTITY_EXPOSER, exposer = IdentityExposer.class)
  private final AtomicInteger callsWithIdentityExposer = new AtomicInteger();

  @Stat(value = CALLS_WITH_INFERENCE_EXPOSER, exposer = InferenceExposer.class)
  private final AtomicInteger callsWithInferenceExposer = new AtomicInteger();

  @Stat(value = CALLS_WITH_TO_STRING_EXPOSER, exposer = ToStringExposer.class)
  private final AtomicInteger callsWithToStringExposer = new AtomicInteger();

  public static final String LIST_WITH_DEFAULT_EXPOSER =
      "list-with-default-exposer";
  public static final String LIST_WITH_IDENTITY_EXPOSER =
      "list-with-identity-exposer";
  public static final String LIST_WITH_INFERENCE_EXPOSER =
      "list-with-inference-exposer";
  public static final String LIST_WITH_TO_STRING_EXPOSER =
      "list-with-to-string-exposer";

  @Stat(LIST_WITH_DEFAULT_EXPOSER)
  private final List<Integer> listWithDefaultExposer = Lists.newArrayList();

  @Stat(value = LIST_WITH_IDENTITY_EXPOSER, exposer = IdentityExposer.class)
  private final List<Integer> listWithIdentityExposer = Lists.newArrayList();

  @Stat(value = LIST_WITH_INFERENCE_EXPOSER, exposer = InferenceExposer.class)
  private final List<Integer> listWithInferenceExposer = Lists.newArrayList();

  @Stat(value = LIST_WITH_TO_STRING_EXPOSER , exposer = ToStringExposer.class)
  private final List<Integer> listWithToStringExposer = Lists.newArrayList();

  /** Increments all counters by one, and adds an element to each list */
  public void call() {
    callsWithDefaultExposer.incrementAndGet();
    callsWithIdentityExposer.incrementAndGet();
    callsWithInferenceExposer.incrementAndGet();
    callsWithToStringExposer.incrementAndGet();

    listWithDefaultExposer.add(callsWithDefaultExposer.get());
    listWithIdentityExposer.add(callsWithIdentityExposer.get());
    listWithInferenceExposer.add(callsWithInferenceExposer.get());
    listWithToStringExposer.add(callsWithToStringExposer.get());
  }

  /** Returns the number of invocations of {@link #call()} */
  public AtomicInteger getCallCount() {
    return new AtomicInteger(callsWithDefaultExposer.get());
  }

  /** Returns the value of each of the lists on this instance. */
  public List<Integer> getCallsList() {
    return Lists.newArrayList(listWithDefaultExposer);
  }
}
