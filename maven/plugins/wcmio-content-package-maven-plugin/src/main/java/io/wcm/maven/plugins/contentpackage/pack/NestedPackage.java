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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

/**
 * The <code>NestedPackage</code> class represents an nested Package dependency
 * from the project descriptor. Such an nested package is declared in
 * <code>&lt;subPackage&gt;</code> elements inside the list style
 * <code>&lt;subPackages&gt;</code> element as follows:
 *
 * <pre>
 * &lt;embedded&gt;
 *   &lt;groupId&gt;artifact.groupId&lt;/groupId&gt;
 *   &lt;artifactId&gt;artifact.artifactId&lt;/artifactId&gt;
 *   &lt;scope&gt;compile&lt;/scope&gt;
 *   &lt;type&gt;jar&lt;/type&gt;
 *   &lt;classifier&gt;sources&lt;/classifier&gt;
 *   &lt;filter&gt;true&lt;/filter&gt;
 * &lt;/embedded&gt;
 * </pre>
 */
public final class NestedPackage extends AbstractAddition {

  List<Artifact> getMatchingArtifacts(final MavenProject project) {
    // Get the dependencies with or without transitives
    final Set<Artifact> dependencies;
    if (excludeTransitive) {
      // only direct dependencies, transitives excluded
      dependencies = project.getDependencyArtifacts();
    }
    else {
      // all dependencies, transitives included
      dependencies = project.getArtifacts();
    }

    final List<Artifact> matches = new ArrayList<Artifact>();
    for (Artifact dependency : dependencies) {
      if (groupId.contains(dependency.getGroupId())
          && artifactId.contains(dependency.getArtifactId())
          && (scope == null || scope.include(dependency))
          && (type == null || type.equals(dependency.getType()))
          && (classifier == null || classifier.equals(dependency.getClassifier()))) {
        matches.add(dependency);
      }
    }
    return matches;
  }

  @Override
  public String toString() {
    return "NestedPackage{" +
        "groupId='" + groupId + '\'' +
        ", artifactId='" + artifactId + '\'' +
        ", scope=" + scope +
        ", type='" + type + '\'' +
        ", classifier='" + classifier + '\'' +
        ", generateFilter=" + generateFilter +
        ", excludeTransitive=" + excludeTransitive +
        '}';
  }

}
