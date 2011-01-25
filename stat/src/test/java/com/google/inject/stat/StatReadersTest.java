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

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * This class includes tests for the logic within {@link StatReaders}.
 *
 * @author ffaber@gmail.com (Fred Faber)
 */
public class StatReadersTest {

  static class TestClass {
    static Integer staticField;

    static Integer getStaticFieldValue() {
      return staticField;
    }

    Integer instanceField;

    Integer getInstanceFieldValue() {
      return instanceField;
    }
  }

  static final Integer STATIC_FIELD_STARTING_VALUE = 1;
  static final Integer INSTANCE_FIELD_STARTING_VALUE = 2;

  Field staticField;
  Method staticMethod;
  Field instanceField;
  Method instanceMethod;
  TestClass testClass;
  
  @Before public void setUp() throws Exception {
    testClass = new TestClass();
    TestClass.staticField = STATIC_FIELD_STARTING_VALUE;
    testClass.instanceField = INSTANCE_FIELD_STARTING_VALUE;

    staticField = TestClass.class.getDeclaredField("staticField");
    staticMethod = TestClass.class.getDeclaredMethod("getStaticFieldValue");
    instanceField = TestClass.class.getDeclaredField("instanceField");
    instanceMethod = TestClass.class.getDeclaredMethod("getInstanceFieldValue");
  }

  @Test public void testInstanceFieldReader() {
    StatReader statReader = StatReaders.forField(instanceField, testClass);
    assertEquals(testClass.instanceField, statReader.readStat());

    // Mutate the field and confirm that its updated value is read.
    testClass.instanceField++;
    assertEquals(testClass.instanceField, statReader.readStat());
  }
  
  @Test public void testStaticFieldReader() { 
    StatReader statReader = StatReaders.forStaticField(staticField);
    assertEquals(TestClass.staticField, statReader.readStat());

    // Mutate the field and confirm that its updated value is read.
    TestClass.staticField++;
    assertEquals(TestClass.staticField, statReader.readStat());
  }
  
  @Test public void testInstanceMethodReader() { 
    StatReader statReader = StatReaders.forMethod(instanceMethod, testClass);
    assertEquals(testClass.getInstanceFieldValue(), statReader.readStat());
    
    // Mutate the field and confirm that its updated value is read.
    testClass.instanceField++;
    assertEquals(testClass.getInstanceFieldValue(), statReader.readStat());
  }
  
  @Test public void testStaticMethodReader() { 
    StatReader statReader = StatReaders.forStaticMethod(staticMethod);
    assertEquals(TestClass.getStaticFieldValue(), statReader.readStat());

    // Mutate the field and confirm that its updated value is read.
    TestClass.staticField++;
    assertEquals(TestClass.getStaticFieldValue(), statReader.readStat());
  }

  @Test public void testInstanceMemberReader_forField() {
    StatReader memberReader = StatReaders.forMember(instanceField, testClass);
    StatReader fieldReader = StatReaders.forField(instanceField, testClass);
    assertEquals(memberReader, fieldReader);
  }
  
  @Test public void testInstanceMemberReader_forMethod() {
    StatReader memberReader = StatReaders.forMember(instanceMethod, testClass);
    StatReader methodReader = StatReaders.forMethod(instanceMethod, testClass);
    assertEquals(memberReader, methodReader);
  }
  
  @Test public void testStaticMemberReader_forField() {
    StatReader memberReader = StatReaders.forStaticMember(staticField);
    StatReader fieldReader = StatReaders.forStaticField(staticField);
    assertEquals(memberReader, fieldReader);
  }
  
  @Test public void testStaticMemberReader_forMember() {
    StatReader memberReader = StatReaders.forStaticMember(staticMethod);
    StatReader methodReader = StatReaders.forStaticMethod(staticMethod);
    assertEquals(memberReader, methodReader );
  }

  @Test public void testObjectReader() {
    List<Integer> statList = Lists.newArrayList(1, 2);
    StatReader statReader = StatReaders.forObject(statList);
    assertEquals(statList, statReader.readStat());

    // If we add a value, we expect it to be reflected in the stat that is read
    statList.add(3);
    assertEquals(statList, statReader.readStat());
  }
}
