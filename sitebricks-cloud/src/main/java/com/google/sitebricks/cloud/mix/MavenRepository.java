package com.google.sitebricks.cloud.mix;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MavenRepository extends MavenDependency {
  private final String id;
  private final String url;
  private boolean releases;
  private boolean snapshots;

  public MavenRepository(String id, String url) {
    this(id, url, true, false);
  }

  public MavenRepository(String id, String url, boolean releases, boolean snapshots) {
    super(null, null, null);

    this.id = id;
    this.url = url;
    this.releases = releases;
    this.snapshots = snapshots;
    setType("repo");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    MavenRepository that = (MavenRepository) o;

    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (url != null ? !url.equals(that.url) : that.url != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (url != null ? url.hashCode() : 0);
    return result;
  }

  @Override
  public String toDepString() {
    return
        "  <repository>\n" +
        "     <id>" + id + "</id>\n" +
        "     <url>" + url + "</url>\n" +
        "     <releases>\n" +
        "       <enabled>" + releases + "</enabled>\n" +
        "     </releases> \n" +
        "     <snapshots>\n" +
        "       <enabled>" + snapshots + "</enabled>\n" +
        "     </snapshots>\n" +
        "   </repository>";
  }
}
