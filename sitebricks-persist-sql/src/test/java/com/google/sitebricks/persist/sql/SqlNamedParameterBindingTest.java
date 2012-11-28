package com.google.sitebricks.persist.sql;

import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class SqlNamedParameterBindingTest {

  @Test
  public final void testNamedParameterBinding() {
    String sql = "select * from blah where name = @name and age = @name or age = @age -- stuff";
    Map<String, Object> map = ImmutableMap.<String, Object>of(
        "name", "Dhanji",
        "age", 32
    );

    Map<Integer, Object> positionalParams = Sql.toPositionalMap(map, Sql.NAMED_ARG_PATTERN
        .matcher(sql));

    assertEquals(positionalParams.get(1), "Dhanji");
    assertEquals(positionalParams.get(2), "Dhanji");
    assertEquals(positionalParams.get(3), 32);
  }

  @Test
  public final void testNamedParameterBindingFailsOnMissingParams() {
    String sql = "select * from blah where name = @name and age = @name or age = @age or @blah -- stuff";
    Map<String, Object> map = ImmutableMap.<String, Object>of(
        "name", "Dhanji",
        "age", 32
    );

    try {
      Map<Integer, Object> positionalParams = Sql.toPositionalMap(map, Sql.NAMED_ARG_PATTERN
          .matcher(sql));
      fail();
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("@blah"));
    }
  }
}
