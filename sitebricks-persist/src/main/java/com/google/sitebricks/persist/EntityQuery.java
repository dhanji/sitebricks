package com.google.sitebricks.persist;

import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public interface EntityQuery<T> {
  <E> Clause<T> where(E field, FieldMatcher<E> matcher);

  public static interface Clause<T> {
    <E> Clause and(E field, FieldMatcher<E> matcher);

    EntityQuery<T> or();

    List<T> list();

    List<T> list(int limit);

    List<T> list(int offset, int limit);

    /**
     * Delete all matching persistent objects from the underlying datastore. Useful
     * for bulk deletions, only datastores that support this natively will work.
     */
    <T> void remove();
  }

  public static class FieldMatcher<E> {
    public final Kind kind;
    public final E low, high;
    public final double threshold;

    protected FieldMatcher(Kind kind, E value) {
      this(kind, value, null, -1.0);
    }

    protected FieldMatcher(Kind kind, E low, E high) {
      this(kind, low, high, -1.0);
    }

    protected FieldMatcher(Kind kind, E low, E high, double threshold) {
      this.kind = kind;
      this.low = low;
      this.high = high;
      this.threshold = threshold;
    }

    public static <T> FieldMatcher<T> is(T t) {
      return new FieldMatcher<T>(Kind.IS, t);
    }

    public static <T> FieldMatcher<T> not(T t) {
      return new FieldMatcher<T>(Kind.NOT, t);
    }

    public static FieldMatcher<String> like(String t) {
      return new FieldMatcher<String>(Kind.LIKE, t);
    }

    /**
     * Adds fuzzy query constraint on the given string matched against a fuzziness threshold.
     * For example:
     * <pre>
     *   similarTo("cafe", 1.0);
     * </pre>
     *
     * May return "cate", "café", "late" etc., depending on the threshold. The threshold is an
     * abstract specifier, which the underlying datastore may choose to incorporate into its
     * algorithm or ignore completely.
     * <p>
     *   A value of 0 of the threshold indicates an exact match, whereas 1.0 indicates an maximally
     *   expansive, fuzzy match.
     * </p>
     */
    public static <String> FieldMatcher<String> similarTo(String t, double threshold) {
      if (threshold > 1.0 || threshold < 0.0)
        throw new IllegalArgumentException("Similarity threshold must be between [0 - 1]");
      return new FieldMatcher<String>(Kind.SIMILAR_TO, t, null, threshold);
    }

    public static <T> FieldMatcher<T> between(T t1, T t2) {
      return new FieldMatcher<T>(Kind.BETWEEN, t1, t2);
    }

    public static <T> FieldMatcher<T> above(T t) {
      return new FieldMatcher<T>(Kind.ABOVE, t);
    }

    public static <T> FieldMatcher<T> below(T t) {
      return new FieldMatcher<T>(Kind.BELOW, t);
    }

    public static <T> FieldMatcher<T> aboveIncluding(T t) {
      return new FieldMatcher<T>(Kind.ABOVE_INCLUDING, t);
    }

    public static <T> FieldMatcher<T> belowIncluding(T t) {
      return new FieldMatcher<T>(Kind.BELOW_INCLUDING, t);
    }

    public static enum Kind {
      IS, NOT, LIKE, SIMILAR_TO, ABOVE, BELOW, ABOVE_INCLUDING, BELOW_INCLUDING, BETWEEN,
      CUSTOM,
    }
  }
}
