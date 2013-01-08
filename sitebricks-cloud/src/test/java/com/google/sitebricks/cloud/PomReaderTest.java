package com.google.sitebricks.cloud;

import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

import java.io.InputStreamReader;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class PomReaderTest {
  @Test
  public final void testReadEnvironment() throws Exception {
    Map<String, String> env = ProcRunner.readEnvironment(
        new InputStreamReader(PomReaderTest.class.getResourceAsStream("pom.xml")),
        "local");

    assertEquals(ImmutableMap.of(
        "hello", "there",
        "second", "prop"
    ), env);
  }
}
