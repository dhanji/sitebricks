package com.google.sitebricks.persist.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  public void execute(String sql, Map<String, Object> params) {
    try {

      String[] matcher = NAMED_ARG_PATTERN.split(sql);
//      PreparedStatement statement = connection.prepareStatement(sql);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  public ResultSet query(String sql, Map<String, Object> params) {
    return null;
  }

  public List<Map<String, Object>> list(String sql, Map<String, Object> params) {
    ResultSet resultSet = query(sql, params);

    try {
      ResultSetMetaData metaData = resultSet.getMetaData();
      List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
      int count = metaData.getColumnCount();

      while (resultSet.next()) {
        Map<String, Object> row = new HashMap<String, Object>();
        for (int i = 0; i < count; i++) {
          String column = metaData.getColumnName(i);

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
