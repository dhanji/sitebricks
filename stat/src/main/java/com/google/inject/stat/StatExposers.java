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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class contains fundamental implementations of {@link StatExposer}, and
 * offers static methods to obtain instances thereof.
 *
 * @author ffaber@gmail.com (Fred Faber)
 */
public final class StatExposers {
  private StatExposers() { }

  /**
   * This exposure performs some basic inferences on a value to determine
   * how it should expose the value as an equivalent.  As a default case, it
   * returns the string value of the stat.
   */
  public final static class InferenceExposer
      implements StatExposer {
    @SuppressWarnings("unchecked")
    @Override public <T> Object expose(T target) {
      if (target instanceof List) {
        return Collections.unmodifiableList((List) target);
      }
      if (target instanceof Map) {
        return Collections.unmodifiableMap((Map) target);
      }
      if (target instanceof Set) {
        return Collections.unmodifiableSet((Set) target);
      }
      return String.valueOf(target);
    }
  }

  /** This exposer returns the string value of a stat as its exposed form. */
  public final static class ToStringExposer implements StatExposer {
    @Override public <T> Object expose(T target) {
      return String.valueOf(target);
    }
  }

  /** This exposer returns the raw stat it is passed as its exposed form. */
  public final static class IdentityExposer implements StatExposer {
    @Override public <T> Object expose(T target) {
      return target;
    }
  }
}
