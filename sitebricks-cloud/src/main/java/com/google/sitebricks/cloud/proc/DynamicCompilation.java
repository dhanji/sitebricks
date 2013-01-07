package com.google.sitebricks.cloud.proc;

import com.google.sitebricks.cloud.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class DynamicCompilation {
  private static final Logger log = LoggerFactory.getLogger("sitebricks");

  public static void compile(Config config) throws Exception {
    log.info("compiling via maven");
    Proc proc = new Proc("maven: mvn package", config, true);
    proc.start(new String[] {});
    proc.stop();    // not synchronous.
    if (proc.await() != 0) {
      log.warn("compile failed.");
      proc.dumpBuffer();
    }
    log.info("compile succeeded");
  }
}
