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

import com.google.common.base.Objects;

import java.lang.reflect.Member;

/**
 * This is a value object that contains information about a member
 * annotated with {@link Stat}.
 *
 * @author ffaber@gmail.com (Fred Faber)
 */
class StatAnnotatedMember {
  final Stat stat;
  final Member member;

  StatAnnotatedMember(Stat stat, Member member) {
    this.stat = stat;
    this.member = member;
  }

  Stat getStat() {
    return stat;
  }

  @SuppressWarnings("unchecked")
  <T extends Member> T getMember() {
    return (T) member;
  }

  @Override public boolean equals(Object other) {
    if (!(other instanceof StatAnnotatedMember)) {
      return false;
    }

    StatAnnotatedMember otherAnnotatedMember = (StatAnnotatedMember) other;
    return Objects.equal(this.member, otherAnnotatedMember.member)
        && Objects.equal(this.stat, otherAnnotatedMember.stat);
  }

  @Override public int hashCode() {
    return Objects.hashCode(stat, member);
  }

  @Override public String toString() {
    return Objects.toStringHelper(this)
        .add("Stat", stat)
        .add("Member", member)
        .toString();
  }
}
