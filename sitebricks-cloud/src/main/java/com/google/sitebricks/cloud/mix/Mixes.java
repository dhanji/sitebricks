package com.google.sitebricks.cloud.mix;

import com.google.common.collect.ImmutableSet;
import com.google.sitebricks.cloud.Command;
import com.google.sitebricks.cloud.Config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Mixes implements Command {
  private static final Map<String, Mix> mixes = new HashMap<String, Mix>();
  public static final Set<String> DEFAULT_MIXES = ImmutableSet.of(
      "@web",
      "@jetty",
      "@test",
      "@procfile"
  );
  static {
    // TODO Guicify all of these.
    mixes.put("@sql", new SqlMix());
    mixes.put("@mysql", new MySqlMix());
    mixes.put("@postgresql", new PostgresMix());
    mixes.put("@postgres", new PostgresMix());
    mixes.put("@redis", new RedisMix());
    mixes.put("@persist", new PersistMix());
    mixes.put("@test", new TestMix());
    mixes.put("@websocket", new LauncherMix());
    mixes.put("@procfile", new LauncherMix());
    mixes.put("@jetty", new JettyMix());
    mixes.put("@web", new WebMix());
    mixes.put("@mail", new MailMix());
    mixes.put("@resource", new ResourceMix());
  }

  public static Mix get(String name) {
    return mixes.get(name);
  }

  @Override
  public void run(List<String> commands, Config config) throws Exception {
    System.out.println("Available component mixes:");
    for (String mix : mixes.keySet()) {
      System.out.println("  " + mix);
    }
    System.out.println();
  }
}
