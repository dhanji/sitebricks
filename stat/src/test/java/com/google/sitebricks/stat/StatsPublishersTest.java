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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.sitebricks.stat.StatsPublishers.HtmlStatsPublisher;
import com.google.sitebricks.stat.StatsPublishers.JsonStatsPublisher;
import com.google.sitebricks.stat.StatsPublishers.TextStatsPublisher;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.testng.Assert.assertEquals;


/**
 * This test class contains tests for the various types of
 * {@link StatsPublisher publishers} that are defined within the
 * {@code com.google.inject.stat} package.
 *
 * @author ffaber@gmail.com (Fred Faber)
 */
public class StatsPublishersTest {
  private static final String NL = System.getProperty("line.separator");
  
  StringWriter stringWriter;
  PrintWriter printWriter;
  ImmutableMap<StatDescriptor, Object> snapshot =
      ImmutableMap.<StatDescriptor, Object>builder()
        .put(StatDescriptor.of("int-stat", "", null, null), 3)
        .put(StatDescriptor.of("float-stat", "", null, null), 4.3d)
        .put(StatDescriptor.of("list-stat", "", null, null),
            ImmutableList.of("a", "b", "c"))
        .build();

  @BeforeMethod
  public final void before() {
    stringWriter = new StringWriter(1024);
    printWriter = new PrintWriter(stringWriter);
  }

  @Test
  public void testHtmlPublisher() {
    HtmlStatsPublisher publisher = new HtmlStatsPublisher();
    String expectedOutput = new StringBuilder()
        .append("<html><head><style>").append(NL)
        .append("body { font-family: monospace; }").append(NL)
        .append("</style></head><body>").append(NL)
        .append("<b>int-stat:</b> 3<br/>").append(NL)
        .append("<b>float-stat:</b> 4.3<br/>").append(NL)
        .append("<b>list-stat:</b> [a, b, c]<br/>").append(NL)
        .append("</body></html>").append(NL)
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
        .append("int-stat 3").append(NL)
        .append("float-stat 4.3").append(NL)
        .append("list-stat [a, b, c]").append(NL)
        .toString();
    assertPublishing(publisher, expectedOutput);
  }

  private void assertPublishing(
      StatsPublisher publisher, String expectedOutput) {
    publisher.publish(snapshot, printWriter);
    printWriter.flush();
    assertEquals(expectedOutput, stringWriter.getBuffer().toString());
  }
}
