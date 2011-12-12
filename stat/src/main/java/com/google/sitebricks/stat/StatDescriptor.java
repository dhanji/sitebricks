package com.google.sitebricks.stat;

import com.google.common.base.Objects;

/**
 * A {@link StatDescriptor} encapsulates the information required to publish
 * a stat.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public final class StatDescriptor {
  private final String name;
  private final String description;
  private final StatReader statReader;
  private final Class<? extends StatExposer> statExposerClass;

  private StatDescriptor(
      String name, String description, StatReader statReader,
      Class<? extends StatExposer> statExposerClass) {
    this.name = name;
    this.description = description;
    this.statReader = statReader;
    this.statExposerClass = statExposerClass;
  }

  static StatDescriptor of(
      String name, String description, StatReader statReader,
      Class<? extends StatExposer> statExposerClass) {
    return new StatDescriptor(name, description, statReader, statExposerClass);
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public StatReader getStatReader() {
    return statReader;
  }

  public Class<? extends StatExposer> getStatExposerClass() {
    return statExposerClass;
  }

  @Override public String toString() {
    return Objects.toStringHelper(this)
        .add("name", name)
        .add("description", description)
        .add("statReader", statReader)
        .add("statExposerClass", statExposerClass)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StatDescriptor)) {
      return false;
    }

    StatDescriptor that = (StatDescriptor) o;
    return Objects.equal(this.name, that.name)
        && Objects.equal(this.description, that.description)
        && Objects.equal(this.statReader, that.statReader)
        && Objects.equal(this.statExposerClass, that.statExposerClass);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, description, statReader, statExposerClass);
  }
}
