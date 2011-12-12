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

/**
 * A {@link StatReader} is ble to read the value of a stat, be it a direct
 * reference to an object, a field or method on a class, or by some other
 * means.
 * <p>
 * To create an instance of this class, use the factory methods defined
 * on {@link StatReaders}.
 *
 * @author ffaber@gmail.com (Fred Faber)
 */
public abstract class StatReader {
  StatReader() { }

  /** Returns the value of the stat. */
  public abstract Object readStat();

  abstract boolean equalsOtherStatReader(StatReader otherStatReader);

  abstract int hashCodeForStatReader();

  @Override public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof StatReader)) {
      return false;
    }

    return equalsOtherStatReader((StatReader) o);
  }

  @Override public int hashCode() {
    return hashCodeForStatReader();
  }
}
