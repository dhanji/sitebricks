package com.google.sitebricks.cloud.mix;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MavenDependency {
  private String group;
  private String artifact;
  private String version;
  private String classifier;
  private String scope;

  public MavenDependency(String group, String artifact, String version) {
    this.group = group;
    this.artifact = artifact;
    this.version = version;
  }

  public MavenDependency(String group, String artifact, String version, String classifier,
                         String scope) {
    this.group = group;
    this.artifact = artifact;
    this.version = version;
    this.classifier = classifier;
    this.scope = scope;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MavenDependency that = (MavenDependency) o;

    if (!artifact.equals(that.artifact)) return false;
    if (!group.equals(that.group)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = group.hashCode();
    result = 31 * result + artifact.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return group + ":" + artifact + ":" + version
        + (classifier != null ? "(" + classifier + ")" : "")
        + (scope != null ? " => " + scope : "");
  }

  public String toDepString() {
    return "    <dependency>\n"
        +  "      <groupId>" + group + "</groupId>\n"
        +  "      <artifactId>" + artifact + "</artifactId>\n"
        +  "      <version>" + version + "</version>\n"
        + (classifier != null ? "      <classifier>" + classifier + "</classifier>\n" : "")
        + (scope != null ?      "      <scope>" + scope + "</scope>\n" : "")
        +  "    </dependency>\n";
  }
}
