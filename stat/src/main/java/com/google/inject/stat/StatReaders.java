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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * This class offers methods to obtain instances of {@link StatReader}.
 *
 * @author ffaber@gmail.com (Fred Faber)
 */
public final class StatReaders {
  private StatReaders() { }

  public static StatReader forField(final Field field, final Object target) {
    return new FieldBasedStatReader(field, target);
  }

  public static StatReader forStaticField(Field field) {
    return forField(field, null);
  }

  public static StatReader forMethod(
      final Method method, final Object target) {
    return new MethodBasedStatReader(method, target);
  }

  public static StatReader forStaticMethod(Method method) {
    return forMethod(method, null);
  }

  public static StatReader forStaticMember(Member member) {
    return forMember(member, null);
  }

  public static StatReader forMember(Member member, Object target) {
    if (member instanceof Field) {
      return forField((Field) member, target);
    }
    if (member instanceof Method) {
      return forMethod((Method) member, target);
    }
    throw new IllegalArgumentException("Unsupported type of member: " + member);
  }

  public static StatReader forObject(Object object) {
    return new ObjectBasedStatReader(object);
  }

  private static class FieldBasedStatReader extends StatReader {
    private final Field field;
    private final Object target;

    FieldBasedStatReader(Field field, Object target) {
      this.field = field;
      this.target = target;
    }

    @Override public Object readStat() {
      if (!field.isAccessible()) {
        field.setAccessible(true);
      }
      try {
        return field.get(target);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }

    @Override boolean equalsOtherStatReader(StatReader otherStatReader) {
      if (!FieldBasedStatReader.class.isInstance(otherStatReader)) {
        return false;
      }
      FieldBasedStatReader that =
          (FieldBasedStatReader) otherStatReader;
      return Objects.equal(this.field, that.field)
          && Objects.equal(this.target, that.target);
    }

    @Override int hashCodeForStatReader() {
      return Objects.hashCode(field, target);
    }

    @Override public String toString() {
      return Objects.toStringHelper(this)
          .add("field", field)
          .add("target", target)
          .toString();
    }
  }

  private static class MethodBasedStatReader extends StatReader {
    private final Method method;
    private final Object target;

    MethodBasedStatReader(Method method, Object target) {
      this.method = method;
      this.target = target;
    }

    @Override public Object readStat() {
      if (!method.isAccessible()) {
        method.setAccessible(true);
      }
      try {
        return method.invoke(target);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }

    @Override boolean equalsOtherStatReader(StatReader otherStatReader) {
      if (!MethodBasedStatReader.class.isInstance(otherStatReader)) {
        return false;
      }
      MethodBasedStatReader that =
          (MethodBasedStatReader) otherStatReader;
      return Objects.equal(this.method, that.method)
          && Objects.equal(this.target, that.target);
    }

    @Override int hashCodeForStatReader() {
      return Objects.hashCode(method, target);
    }

    @Override public String toString() {
      return Objects.toStringHelper(this)
          .add("method", method)
          .add("target", target)
          .toString();
    }
  }

  private static class ObjectBasedStatReader extends StatReader {
    private final Object object;

    ObjectBasedStatReader(Object object) {
      checkNotNull(object);
      this.object = object;
    }

    @Override public Object readStat() {
      return object;
    }

    @Override boolean equalsOtherStatReader(StatReader otherStatReader) {
      if (!ObjectBasedStatReader.class.isInstance(otherStatReader)) {
        return false;
      }
      ObjectBasedStatReader that =
          (ObjectBasedStatReader) otherStatReader;
      return this.object.equals(that.object);
    }

    @Override int hashCodeForStatReader() {
      return object.hashCode();
    }

    @Override public String toString() {
      return Objects.toStringHelper(this)
          .add("object", object)
          .toString();
    }
  }
}
