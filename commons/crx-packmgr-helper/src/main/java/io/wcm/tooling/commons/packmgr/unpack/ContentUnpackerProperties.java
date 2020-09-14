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
package io.wcm.tooling.commons.packmgr.unpack;

/**
 * Configuration properties for {@link ContentUnpacker}.
 */
public final class ContentUnpackerProperties {

  private String[] excludeFiles;
  private String[] excludeNodes;
  private String[] excludeProperties;
  private String[] excludeMixins;
  private boolean markReplicationActivated;
  private String[] markReplicationActivatedIncludeNodes;
  private String dateLastReplicated;

  /**
   * Exclude files
   * @return Name patterns
   */
  public String[] getExcludeFiles() {
    return this.excludeFiles;
  }

  public void setExcludeFiles(String[] excludeFiles) {
    this.excludeFiles = excludeFiles;
  }

  /**
   * Exclude nodes
   * @return Name patterns
   */
  public String[] getExcludeNodes() {
    return this.excludeNodes;
  }

  public void setExcludeNodes(String[] excludeNodes) {
    this.excludeNodes = excludeNodes;
  }

  /**
   * Exclude properties
   * @return Name patterns
   */
  public String[] getExcludeProperties() {
    return this.excludeProperties;
  }

  public void setExcludeProperties(String[] excludeProperties) {
    this.excludeProperties = excludeProperties;
  }

  /**
   * Exclude mixins
   * @return Name patterns
   */
  public String[] getExcludeMixins() {
    return this.excludeMixins;
  }

  public void setExcludeMixins(String[] excludeMixins) {
    this.excludeMixins = excludeMixins;
  }

  /**
   * Set replication status to "activated" for all cq:Page and cq:Template nodes.
   * @return Set replication status
   */
  public boolean isMarkReplicationActivated() {
    return this.markReplicationActivated;
  }

  public void setMarkReplicationActivated(boolean markReplicationActivated) {
    this.markReplicationActivated = markReplicationActivated;
  }

  /**
   * Node path filter expressions to apply "activated" status on.
   * @return Path patterns
   */
  public String[] getMarkReplicationActivatedIncludeNodes() {
    return this.markReplicationActivatedIncludeNodes;
  }

  public void setMarkReplicationActivatedIncludeNodes(String[] markReplicationActivatedIncludeNodes) {
    this.markReplicationActivatedIncludeNodes = markReplicationActivatedIncludeNodes;
  }

  /**
   * Sets a fixed date to be used for the "lastReplicated" property when setting replication status to "activated".
   * If not set the current date is used.
   * @return Date in ISO8601 format. Example: <code>2020-01-01T00:00:00.000+02:00</code>.
   */
  public String getDateLastReplicated() {
    return this.dateLastReplicated;
  }

  public void setDateLastReplicated(String dateLastReplicated) {
    this.dateLastReplicated = dateLastReplicated;
  }

}
