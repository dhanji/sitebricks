package com.google.sitebricks.cloud.mix;

import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Mixes {
  private static final Map<String, Mix> mixes = new HashMap<String, Mix>();
  public static final Set<String> DEFAULT_MIXES = ImmutableSet.of(
      "@web",
      "@jetty",
      "@test",
      "@launcher"
  );
  static {
    mixes.put("@sql", new SqlMix());
    mixes.put("@mysql", new MySqlMix());
    mixes.put("@postgresql", new PostgresMix());
    mixes.put("@postgres", new PostgresMix());
    mixes.put("@redis", new RedisMix());
    mixes.put("@persist", new PersistMix());
    mixes.put("@test", new TestMix());
    mixes.put("@websocket", new LauncherMix());
    mixes.put("@launcher", new LauncherMix());
    mixes.put("@jetty", new JettyMix());
    mixes.put("@web", new WebMix());
  }

  public static Mix get(String name) {
    return mixes.get(name);
  }
}
