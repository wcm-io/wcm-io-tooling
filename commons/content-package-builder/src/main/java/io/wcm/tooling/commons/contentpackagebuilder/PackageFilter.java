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
package io.wcm.tooling.commons.contentpackagebuilder;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Root path filter for package defintion.
 */
public final class PackageFilter {

  private final String rootPath;
  private final List<PackageFilterRule> rules = new ArrayList<>();

  /**
   * @param rootPath Root path
   */
  public PackageFilter(String rootPath) {
    this.rootPath = rootPath;
  }

  /**
   * @return Root path
   */
  public String getRootPath() {
    return this.rootPath;
  }

  /**
   * Add include rule
   * @param pattern Rule pattern
   * @return this
   */
  public PackageFilter addIncludeRule(String pattern) {
    rules.add(new PackageFilterRule(pattern, true));
    return this;
  }

  /**
   * Add exclude rule
   * @param pattern Rule pattern
   * @return this
   */
  public PackageFilter addExcludeRule(String pattern) {
    rules.add(new PackageFilterRule(pattern, false));
    return this;
  }

  /**
   * @return Get include/exclude rules
   */
  public List<PackageFilterRule> getRules() {
    return ImmutableList.copyOf(rules);
  }

}
