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

package com.google.sitebricks.stat;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.FileAssert.fail;


/**
 * This class includes tests for the various implementations of
 * {@link StatExposer} defined within the {@link StatExposers} class.
 *
 */
public final class StatExposersTest {

  @SuppressWarnings("unchecked")
  @Test
  public void testInferenceExposer() {
    StatExposers.InferenceExposer inferenceExposer = new StatExposers.InferenceExposer();

    List<Integer> rawList = Lists.newArrayList(1, 2, 3);
    List<Integer> exposedList = (List<Integer>) inferenceExposer.expose(rawList);
    assertEquals(rawList, exposedList);
    try {
      exposedList.add(4);
      fail("Should not be able to modify the exposed value");
    } catch (UnsupportedOperationException expected) { 
    }
    try {
      exposedList.clear();
      fail("Should not be able to modify the exposed value");
    } catch (UnsupportedOperationException expected) {
    }

    Map<String, Integer> rawMap = new HashMap<String, Integer>() {{
      put("1", 1);
      put("2", 2);
      put("3", 3);
    }};
    Map<String, Integer> exposedMap = 
        (Map<String, Integer>) inferenceExposer.expose(rawMap);
    assertEquals(rawMap, exposedMap);
    try {
      exposedMap.put("4", 4);
      fail("Should not be able to modify the exposed value");
    } catch (UnsupportedOperationException expected) { 
    }
    try {
      exposedMap.remove("3");
      fail("Should not be able to modify the exposed value");
    } catch (UnsupportedOperationException expected) {
    }

    Set<Integer> rawSet = Sets.newHashSet(1, 2, 3);
    Set<Integer> exposedSet = (Set<Integer>) inferenceExposer.expose(rawSet);
    assertEquals(rawSet, exposedSet);
    try {
      exposedSet.add(4);
      fail("Should not be able to modify the exposed value");
    } catch (UnsupportedOperationException expected) { 
    }
    try {
      exposedSet.remove(3);
      fail("Should not be able to modify the exposed value");
    } catch (UnsupportedOperationException expected) {
    }

    AtomicInteger rawAtomicInteger = new AtomicInteger(4);
    String exposedAtomicInteger =
        (String) inferenceExposer.expose(rawAtomicInteger);
    assertEquals(String.valueOf(rawAtomicInteger.get()), exposedAtomicInteger);
  }

  @Test public void testToStringExposer() {
    StatExposers.ToStringExposer toStringExposer = new StatExposers.ToStringExposer();
    List<Integer> rawList = Lists.newArrayList(1, 2, 3);
    String exposedList = (String) toStringExposer.expose(rawList);
    assertEquals(rawList.toString(), exposedList);
  }

  @SuppressWarnings("unchecked")
  @Test public void testIdentityExposer() {
    StatExposers.IdentityExposer identityExposer = new StatExposers.IdentityExposer();
    List<Integer> rawList = Lists.newArrayList(1, 2, 3);
    List<Integer> exposedList = (List<Integer>) identityExposer.expose(rawList);
    assertSame(rawList, exposedList);
  }
}
