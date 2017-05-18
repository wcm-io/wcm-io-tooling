/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * The <code>EmbeddedBundle</code> class represents an embedded bundle dependency
 * from the project descriptor. An embedded bundle is declared as child <code>&lt;embedded&gt;</code>
 * in the parent <code>&lt;embeddeds&gt;</code> like this:
 * <p>
 *
 * <pre>
 * &lt;embedded&gt;
 *      &lt;groupId&gt;artifact.groupId&lt;/groupId&gt;
 *      &lt;artifactId&gt;artifact.artifactId&lt;/artifactId&gt;
 *      &lt;scope&gt;compile&lt;/scope&gt;
 *     &lt;type&gt;jar&lt;/type&gt;
 *     &lt;classifier&gt;sources&lt;/classifier&gt;
 *      &lt;filter&gt;true&lt;/filter&gt;
 *      &lt;target&gt;/libs/sling/install&lt;/target&gt;
 * &lt;/embedded&gt;
 * </pre>
 */
public class EmbeddedBundle
    extends AbstractAddition {

  /**
   * JCR Location where the Bundle will be installed in
   */
  @Parameter
  private String target;

  /**
   * Name to use for the bundle in the destination
   */
  @Parameter
  private String destFileName;

  public String getDestFileName() {
    return destFileName;
  }

  public void setDestFileName(String destFileName) {
    this.destFileName = destFileName;
  }

  public void setTarget(String target) {
    // need trailing slash
    if (!target.endsWith("/")) {
      target += "/";
    }

    this.target = target;
  }

  public String getTarget() {
    return target;
  }

  public List<Artifact> getMatchingArtifacts(final MavenProject project) {
    // get artifacts depending on whether we exclude transitives or not
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
    for (Artifact artifact : dependencies) {
      if (groupId.contains(artifact.getGroupId())
          && artifactId.contains(artifact.getArtifactId())
          && (scope == null || scope.include(artifact))
          && (type == null || type.equals(artifact.getType()))
          && (classifier == null || classifier.equals(artifact.getClassifier()))) {
        matches.add(artifact);
      }
    }
    return matches;
  }

  @Override
  public String toString() {
    return "EmbeddedBundle{" +
        "groupId='" + groupId + '\'' +
        ", artifactId='" + artifactId + '\'' +
        ", scope=" + scope +
        ", type='" + type + '\'' +
        ", classifier='" + classifier + '\'' +
        ", generateFilter=" + generateFilter +
        ", target='" + target + '\'' +
        ", destFileName='" + destFileName + '\'' +
        ", excludeTransitive=" + excludeTransitive +
        '}';
  }
}
