package com.google.sitebricks.mail.imap;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Queue;

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
  ;

  private static final Logger log = LoggerFactory.getLogger(Flag.class);

  private static BiMap<String, Flag> flagMap = HashBiMap.create(10);
  static {
    flagMap.put("\\seen", SEEN);
    flagMap.put("\\recent", RECENT);
    flagMap.put("\\deleted", DELETED);
    flagMap.put("\\draft", DRAFT);
    flagMap.put("\\flagged", FLAGGED);
    flagMap.put("\\answered", ANSWERED);
    flagMap.put("$forwarded", FORWARDED);
  }

  public static Flag parse(String flag) {
    return flagMap.get(flag.toLowerCase());
  }

  public static String toImap(Flag f) {
    return flagMap.inverse().get(f);
  }

  public static String toImap(EnumSet<Flag> flags) {
    StringBuffer imap = new StringBuffer("FLAGS (");
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
  public static EnumSet<Flag> parseFlagList(Queue<String> tokens) {
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
