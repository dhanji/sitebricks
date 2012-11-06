package com.google.sitebricks.cloud;

import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public interface Command {
  void run(List<String> commands, Config config) throws Exception;
}
