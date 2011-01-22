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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This test class contains tests for the various types of
 * {@link StatsPublisher publishers} that are defined within the
 * {@code com.google.inject.stat} package.
 *
 * @author ffaber@gmail.com (Fred Faber)
 */
public class StatsPublishersTest {

  StringWriter stringWriter;
  PrintWriter printWriter;
  ImmutableMap<StatDescriptor, Object> snapshot;

  @Before public void setUp() {
    stringWriter = new StringWriter(1024);
    printWriter = new PrintWriter(stringWriter);
    snapshot = ImmutableMap.<StatDescriptor, Object>builder()
        .put(StatDescriptor.of(null, "int-stat", "", null), 3)
        .put(StatDescriptor.of(null, "float-stat", "", null), 4.3d)
        .put(StatDescriptor.of(null, "list-stat", "", null),
            ImmutableList.of("a", "b", "c"))
        .build();
  }

  @Test public void testHtmlPublisher() {
    HtmlStatsPublisher publisher = new HtmlStatsPublisher();
    String expectedOutput = new StringBuilder()
        .append("<html><head><style>\n")
        .append("body { font-family: monospace; }\n")
        .append("</style></head><body>\n")
        .append("<b>int-stat:</b> 3<br/>\n")
        .append("<b>float-stat:</b> 4.3<br/>\n")
        .append("<b>list-stat:</b> [a, b, c]<br/>\n")
        .append("</body></html>\n")
        .toString();
    assertPublishing(publisher, expectedOutput);
  }

  @Test public void testJsonPublisher() {
    JsonStatsPublisher publisher = new JsonStatsPublisher();
    String expectedOutput = new StringBuilder()
        .append("{")
        .append("\"int-stat\":3")
        .append(",")
        .append("\"float-stat\":4.3")
        .append(",")
        .append("\"list-stat\":[\"a\",\"b\",\"c\"]")
        .append("}")
        .toString();
    assertPublishing(publisher, expectedOutput);
  }

  @Test public void testTextPublisher() {
    TextStatsPublisher publisher = new TextStatsPublisher();
    String expectedOutput = new StringBuilder()
        .append("int-stat 3\n")
        .append("float-stat 4.3\n")
        .append("list-stat [a, b, c]\n")
        .toString();
    assertPublishing(publisher, expectedOutput);
  }

  private void assertPublishing(StatsPublisher publisher, String expectedOutput) {
    publisher.publish(snapshot, printWriter);
    printWriter.flush();
    assertEquals(expectedOutput, stringWriter.getBuffer().toString());
  }
}
