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

import org.apache.jackrabbit.vault.fs.api.PathFilterSet;

/**
 * List of {@link PathFilterSet}.
 */
public final class Filter {

  private String root;
  private String[] includes;
  private String[] excludes;

  /**
   * @return Root path
   */
  public String getRoot() {
    return this.root;
  }

  /**
   * @param root Root path
   */
  public void setRoot(String root) {
    this.root = root;
  }

  /**
   * @return Include patterns
   */
  public String[] getIncludes() {
    return this.includes;
  }

  /**
   * @param includes Include patterns
   */
  public void setIncludes(String[] includes) {
    this.includes = includes;
  }

  /**
   * @return Exclude patterns
   */
  public String[] getExcludes() {
    return this.excludes;
  }

  /**
   * @param excludes Exclude patterns
   */
  public void setExcludes(String[] excludes) {
    this.excludes = excludes;
  }

}
