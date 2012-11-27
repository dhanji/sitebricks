package com.google.sitebricks.persist.sql;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class SqlApiTest {

  @Test
  public final void testNamedParameterBinding() {
    String sql = "select * from blah where name = @name and age = @name or age = @age -- stuff";
    Matcher matcher = Sql.NAMED_ARG_PATTERN.matcher(sql);

    Map<String, Object> params = ImmutableMap.<String, Object>of(
        "name", "Dhanji",
        "age", 32);

    boolean find = matcher.find();
    Map<Integer, Object> args = new HashMap<Integer, Object>();
    int index = 1;
    while (find) {
      Object value = params.get(matcher.group().substring(1));

      args.put(index, value);
      find = matcher.find(matcher.end());
      index++;
    }

    matcher.reset();
    matcher.replaceFirst()

    System.out.println(args);
  }
}
