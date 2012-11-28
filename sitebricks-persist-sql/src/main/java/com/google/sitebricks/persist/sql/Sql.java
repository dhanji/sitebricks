package com.google.sitebricks.persist.sql;

import com.google.common.collect.ImmutableMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simplified API to execute SQL statements against. This is what the
 * EntityStore itself uses to manage the SQL interface. Use this as a low-level,
 * implementation-specific API, as needed.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Sql {
  static final Pattern NAMED_ARG_PATTERN = Pattern.compile("(@[\\w_]+)",
      Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
  private final Connection connection;

  public Sql(Connection connection) {
    this.connection = connection;
  }

  public void execute(String sql) {
    query(sql);
  }

  public void execute(String sql, Map<String, Object> params) {
    query(sql, params);
  }

  public ResultSet query(String sql) {
    return query(sql, ImmutableMap.<String, Object>of());
  }

  public ResultSet query(String sql, Map<String, Object> params) {
    try {
      Matcher matcher = Sql.NAMED_ARG_PATTERN.matcher(sql);

      Map<Integer, Object> positionalParams = toPositionalMap(params, matcher);

      matcher.reset();
      sql = matcher.replaceAll("?");

      PreparedStatement statement = connection.prepareStatement(sql);
      for (int i = 1; i <= positionalParams.size(); i++) {
        statement.setObject(i, positionalParams.get(i));
      }

      if (statement.execute())
        return statement.getResultSet();
      else
        return null;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static Map<Integer, Object> toPositionalMap(Map<String, Object> params, Matcher matcher) {
    boolean find = matcher.find();
    Map<Integer, Object> positionalParams = new HashMap<Integer, Object>();
    int index = 1;
    while (find) {
      Object value = params.get(matcher.group().substring(1));
      if (value == null) {
        throw new IllegalArgumentException("Named parameter map for SQL statement did" +
            " not contain required parameter: " + matcher.group());
      }

      positionalParams.put(index, value);
      find = matcher.find(matcher.end());
      index++;
    }
    return positionalParams;
  }

  public boolean tableExists(String name) {
    try {
      return connection.getMetaData().getTables(null, null, name.toUpperCase(), null).next();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public List<Map<String, Object>> list(String sql) {
    return list(sql, ImmutableMap.<String, Object>of());
  }

  public List<Map<String, Object>> list(String sql, Map<String, Object> params) {
    ResultSet resultSet = query(sql, params);

    try {
      ResultSetMetaData metaData = resultSet.getMetaData();
      List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
      int count = metaData.getColumnCount();

      while (resultSet.next()) {
        Map<String, Object> row = new HashMap<String, Object>();
        for (int i = 1; i <= count; i++) {
          String column = metaData.getColumnName(i).toLowerCase();

          row.put(column, resultSet.getObject(column));
        }

        list.add(row);
      }

      return list;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns the current underlying JDBC connection.
   */
  public Connection connection() {
    return connection;
  }
}
