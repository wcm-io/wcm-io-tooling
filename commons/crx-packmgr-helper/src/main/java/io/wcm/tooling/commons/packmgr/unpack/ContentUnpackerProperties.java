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
   * @return Name properties
   */
  public String[] getExcludeProperties() {
    return this.excludeProperties;
  }

  public void setExcludeProperties(String[] excludeProperties) {
    this.excludeProperties = excludeProperties;
  }

}
