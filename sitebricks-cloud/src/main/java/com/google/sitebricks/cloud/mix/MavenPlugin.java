package com.google.sitebricks.cloud.mix;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MavenPlugin extends MavenDependency {
  private String extra;

  public MavenPlugin(String group, String artifact, String version) {
    this(group, artifact, version, null);
  }

  public MavenPlugin(String group, String artifact, String version, String extra) {
    super(group, artifact, version);
    this.extra = extra;

    setType("plugin");
  }

  @Override
  public String toDepString() {
    return
        "     <plugin>\n" +
        "       <groupId>" + group + "</groupId>\n" +
        "       <artifactId>" + artifact + "</artifactId>\n" +
        "       <version>" + version + "</version>\n" +
        "       " + extra +
        "     </plugin>";
  }
}
