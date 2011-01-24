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


/**
 * A {@link StatExposer} is responsible for exposing the value of a stat to
 * the stat publishing logic.  This is a layer of transformation that exists
 * so that registered stats may be protected from leaking as mutable references
 * from within the class in which they exist to the stat publishing logic.
 *
 * @author ffaber@gmail.com (Fred Faber)
 */
public interface StatExposer {

  /** Accepts the raw value of a stat, optionally transforms it, and returns
   * an exposed value of the stat.  This exposed value is what is passed to
   * the stat publishing logic.
   */
  <T> Object expose(T target);
}