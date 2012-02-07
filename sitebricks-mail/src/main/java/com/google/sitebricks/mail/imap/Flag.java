package com.google.sitebricks.mail.imap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public enum Flag {
  SEEN,
  RECENT,
  DELETED,
  DRAFT,
  FLAGGED,
  ANSWERED,
  FORWARDED,
  NOT_JUNK,
  JUNK
  ;

  private static final Logger log = LoggerFactory.getLogger(Flag.class);

  private static final Map<String, Flag> flagMap = new HashMap<String, Flag>();
  private static final Map<Flag, String> lookup = new HashMap<Flag, String>();

  static {
    flagMap.put("\\seen", SEEN);
    flagMap.put("\\recent", RECENT);
    flagMap.put("\\deleted", DELETED);
    flagMap.put("\\draft", DRAFT);
    flagMap.put("\\flagged", FLAGGED);
    flagMap.put("\\answered", ANSWERED);
    flagMap.put("$forwarded", FORWARDED);
    flagMap.put("$notjunk", NOT_JUNK);
    flagMap.put("notjunk", NOT_JUNK);
    flagMap.put("junk", JUNK);
    flagMap.put("$junk", JUNK);

    // Build reverse lookup map.
    for (Entry<String, Flag> entry : flagMap.entrySet()) {
      lookup.put(entry.getValue(), entry.getKey());
    }

    // Overrides
    lookup.put(JUNK, "$Junk");
    lookup.put(NOT_JUNK, "$NotJunk");
  }

  public static Flag parse(String flag) {
    return flagMap.get(flag.toLowerCase());
  }

  public static String toImap(Flag f) {
    return lookup.get(f);
  }

  public static String toImap(Set<Flag> flags) {
    StringBuilder imap = new StringBuilder("FLAGS (");
    Iterator<Flag> it = flags.iterator();
    while (it.hasNext()) {
      imap.append(Flag.toImap(it.next()));
      if (it.hasNext())
        imap.append(" ");
    }
    imap.append(")");
    return imap.toString();
  }

  /**
   * @return set of flags, null on failure.
   */
  public static Set<Flag> parseFlagList(Queue<String> tokens) {
    EnumSet<Flag> result = EnumSet.noneOf(Flag.class);
    if (Parsing.matchAnyOf(tokens, "FLAGS") == null)
      return null;
    Parsing.eat(tokens, "(");

    // Check if there are flags to set.
    while (!")".equals(tokens.peek())) {
      String token = tokens.poll();
      Flag flag = Flag.parse(token);
      if (flag != null)
        result.add(flag);
      else log.warn("Unknown flag type encountered {}, ignoring.", token);
    }
    Parsing.eat(tokens, ")");
    return result;
  }
}
