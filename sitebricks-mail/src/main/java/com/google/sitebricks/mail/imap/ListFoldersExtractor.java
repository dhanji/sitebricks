package com.google.sitebricks.mail.imap;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class ListFoldersExtractor implements Extractor<List<String>> {
  private static final Pattern QUOTES = Pattern.compile("(\".*\")");
  private static final String ROOT_PREFIX = "\"/\"";

  @Override
  public List<String> extract(List<String> messages) {
    ImmutableList.Builder<String> builder = ImmutableList.builder();
    for (String message : messages) {
      Matcher matcher = QUOTES.matcher(message);
      if (matcher.find()) {
        String group = matcher.group(1);

        if (group.startsWith(ROOT_PREFIX)) {
          group = group.substring(ROOT_PREFIX.length()).trim();
        }

        // Strip quotes.
        if (group.startsWith("\"")) {
          group = group.substring(1, group.length() - 1);
        }

        // Generally remove leading "/" and stripquotes
        builder.add(group);
      }
    }

    return builder.build();
  }
}
