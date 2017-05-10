package io.wcm.maven.plugins.contentpackage.pack;

import org.apache.jackrabbit.vault.packaging.VersionRange;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

/**
 * A dependency of the package which is added to the properties.xml
 * file
 */
public class Dependency {

  /**
   * The group of the package dependency
   */
  @Parameter(
    required = true
  )
  private String group;
  /**
   * The name of the package dependency
   */
  @Parameter(
    required = true
  )
  private String name;
  /**
   * The version range
   */
  @Parameter
  private VersionRange versionRange;

  public String getGroup() {
    return group;
  }

  public Dependency setGroup(String group) {
    this.group = group;
    return this;
  }

  public String getName() {
    return name;
  }

  public Dependency setName(String name) {
    this.name = name;
    return this;
  }

  public VersionRange getVersion() {
    return versionRange;
  }

  public Dependency setVersion(String version) {
    this.versionRange = VersionRange.fromString(version);
    return this;
  }

  @Override
  public String toString() {
    return "Dependency{" +
      "group='" + group + '\'' +
      ", name='" + name + '\'' +
      ", versionRange=" + versionRange +
      '}';
  }

  public String toVaultPropertyDependency() {
    return group + ":" + name +
      (versionRange != null ? ":" + versionRange.toString() : "");
  }

  public static String toString(List<Dependency> dependencies) {
    boolean first = true;
    StringBuilder b = new StringBuilder();
    for (Dependency dependency: dependencies) {
      if(first) {
        first = false;
      } else {
        b.append(",");
      }
      b.append(dependency.toVaultPropertyDependency());
    }
    return b.toString();
  }
}
