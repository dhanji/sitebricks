package com.google.sitebricks.mail.imap;

import org.testng.v6.Maps;

import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public enum Flag {
  SEEN,
  RECENT,
  ;
  private static Map<String, Flag> flagMap = Maps.newHashMap();
  static {
    flagMap.put("\\seen", SEEN);
    flagMap.put("\\recent", RECENT);
  }

  public static Flag parse(String flag) {
    return flagMap.get(flag.toLowerCase());
  }
}
