/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2017 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.maven.plugins.contentpackage.pack;

import java.util.List;

import org.apache.jackrabbit.vault.packaging.VersionRange;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * A dependency of the package which is added to the properties.xml
 * file
 */
public final class Dependency {

  /**
   * The group of the package dependency
   */
  @Parameter(
      required = true)
  private String group;

  /**
   * The name of the package dependency
   */
  @Parameter(
      required = true)
  private String name;

  /**
   * The version range
   */
  @Parameter
  private VersionRange versionRange;

  /**
   * @return The group of the package dependency
   */
  public String getGroup() {
    return group;
  }

  /**
   * @param value The group of the package dependency
   * @return this
   */
  public Dependency setGroup(String value) {
    this.group = value;
    return this;
  }

  /**
   * @return The name of the package dependency
   */
  public String getName() {
    return name;
  }

  /**
   * @param value The name of the package dependency
   * @return this
   */
  public Dependency setName(String value) {
    this.name = value;
    return this;
  }

  /**
   * @return The version range
   */
  public VersionRange getVersion() {
    return versionRange;
  }

  /**
   * @param value The version range
   * @return this
   */
  public Dependency setVersion(String value) {
    this.versionRange = VersionRange.fromString(value);
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

  private String toVaultPropertyDependency() {
    return group + ":" + name +
        (versionRange != null ? ":" + versionRange.toString() : "");
  }

  static String toString(List<Dependency> dependencies) {
    boolean first = true;
    StringBuilder b = new StringBuilder();
    for (Dependency dependency : dependencies) {
      if (first) {
        first = false;
      }
      else {
        b.append(",");
      }
      b.append(dependency.toVaultPropertyDependency());
    }
    return b.toString();
  }

}
