package com.google.sitebricks.http;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import java.util.Collection;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Parameters {
  public static Multimap<String, String> readMatrix(String uri) {
    // Do the matrix parameters now.
    ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
    String[] pieces = uri.split("[/]+");
    for (String piece : pieces) {
      String[] pairs = piece.split("[;]+");

      for (String pair : pairs) {
        String[] singlePair = pair.split("[=]+");
        if (singlePair.length > 1) {
          builder.put(singlePair[0], singlePair[1]);
        }
      }
    }

    return builder.build();
  }

  public static String singleMatrixParam(String name, Collection<String> values) {
    if (values.size() > 1) {
      throw new IllegalStateException("This matrix parameter has multiple values, "
          + name + "=" + values);
    }
    return values.isEmpty() ? null : Iterables.getOnlyElement(values);
  }
}
