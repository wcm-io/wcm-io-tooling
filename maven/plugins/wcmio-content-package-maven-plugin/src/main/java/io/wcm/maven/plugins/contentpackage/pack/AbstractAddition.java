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

import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Common Properties for Embedded Bundles and Nested Packages
 */
abstract class AbstractAddition {

  /**
   * The Group Id of the Addition
   */
  protected String groupId;

  /**
   * Artifact Id of the Addition
   */
  protected String artifactId;

  @Parameter
  protected ScopeArtifactFilter scope;

  @Parameter
  protected String type;

  @Parameter
  protected String classifier;

  @Parameter
  protected boolean excludeTransitive;

  /**
   * If <code>true</code> a filter entry will be generated for this entry
   */
  @Parameter
  protected boolean generateFilter;

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public void setScope(String scope) {
    this.scope = new ScopeArtifactFilter(scope);
  }

  public void setGenerateFilter(boolean generateFilter) {
    this.generateFilter = generateFilter;
  }

  public boolean isGenerateFilter() {
    return generateFilter;
  }

  public void setExcludeTransitive(boolean excludeTransitive) {
    this.excludeTransitive = excludeTransitive;
  }

  public boolean isExcludeTransitive() {
    return excludeTransitive;
  }

}
