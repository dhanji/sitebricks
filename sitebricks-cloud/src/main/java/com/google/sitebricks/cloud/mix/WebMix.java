package com.google.sitebricks.cloud.mix;

import com.google.sitebricks.cloud.Cloud;

import javax.inject.Inject;
import java.util.Map;
import java.util.Set;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class WebMix implements Mix {
  @Inject
  ResourceMix resourceMix;

  @Override
  public void mix(Map<String, Object> properties, Set<MavenDependency> deps) throws Exception {
    deps.add(new MavenDependency("javax.servlet", "servlet-api", "2.5", null, "provided"));
    deps.add(new MavenDependency("com.google.sitebricks", "sitebricks", Cloud.SB_VERSION));

    // Are we on the bleeding edge?
    if (Cloud.SB_VERSION.contains("SNAPSHOT")) {
      deps.add(new MavenRepository("sonatype-google-snapshots",
          "http://oss.sonatype.org/content/repositories/google-snapshots/", false, true));
    }

    String packagePath = properties.get("packagePath").toString();

    // Write web.xml if missing.
    Cloud.mkdir("web");
    Cloud.mkdir("web/WEB-INF");
    Cloud.mkdir("src/" + packagePath + "/web");

    Cloud.writeTemplate("web.xml", "web/WEB-INF/web.xml", properties);

    Cloud.writeTemplate("AppConfig.java", "src/" + packagePath + "/AppConfig.java", properties);
    Cloud.writeTemplate("Main.java", "src/" + packagePath + "/Main.java", properties);

    // Now create a hello world for the start page.
    Cloud.writeTemplate("Start.java", "src/" + packagePath + "/web/Start.java", properties);
  }
}
