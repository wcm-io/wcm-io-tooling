package io.wcm.maven.plugins.contentpackage.pack;

import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Common Properties for Embedded Bundles and Nested Packages
 */
public class AbstractAddition {
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
