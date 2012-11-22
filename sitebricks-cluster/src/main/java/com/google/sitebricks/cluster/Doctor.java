package com.google.sitebricks.cluster;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class Doctor implements Command {
  private static final Pattern DIR_REGEX = Pattern.compile("[_$\\w][_$@.\\w\\d]+");
  private volatile List<String> messages = new ArrayList<String>();

  @Override
  public void run(List<String> commands, Config config) {
    try {
      // check app.yml
      File appYaml = new File("app.yml");
      check(appYaml.exists(), "Not a valid sitebricks project. Hint: run 'sitebricks init <project>'");

      Yaml yaml = new Yaml();
      @SuppressWarnings("unchecked")
      Map<String, Object> appConfig = (Map<String, Object>) yaml.load(new FileReader(appYaml));
      check(!appConfig.isEmpty(), "No app types are defined in app.yml! Define a new process");

      for (Map.Entry<String, Object> entry : appConfig.entrySet()) {
        check(DIR_REGEX.matcher(entry.getKey()).matches(),
            "Invalid app name (must not have special characters or spaces): " + entry.getKey());
        @SuppressWarnings("unchecked")
        Map<String, Object> appValue = (Map<String, Object>) entry.getValue();

        check(appValue.containsKey("main"), "App definition is missing 'main' declaration: "
            + entry.getKey());
      }

    } catch (Exception e) {
      // Keep going until we can't anymore.
      if (messages.isEmpty())
        throw new RuntimeException(e);

      for (int i = 0, messagesSize = messages.size(); i < messagesSize; i++) {
        String message = messages.get(i);
        System.out.print(i + 1 + ") " + message);
      }
    }
  }

  private void check(boolean test, String message) {
    if (test)
      return;

    messages.add(message);
  }
}
