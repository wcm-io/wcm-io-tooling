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

/**
 * Include/exclude rule for package filter.
 */
public final class PackageFilterRule {

  private final String pattern;
  private final boolean include;

  PackageFilterRule(String pattern, boolean include) {
    this.pattern = pattern;
    this.include = include;
  }

  /**
   * @return Rule pattern
   */
  public String getPattern() {
    return this.pattern;
  }

  /**
   * @return Is include rule
   */
  public boolean isInclude() {
    return this.include;
  }

  /**
   * @return Is exclude rule
   */
  public boolean isExclude() {
    return !this.include;
  }

}
