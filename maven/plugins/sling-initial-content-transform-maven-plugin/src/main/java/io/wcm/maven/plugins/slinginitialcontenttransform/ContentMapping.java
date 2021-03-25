/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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
package io.wcm.maven.plugins.slinginitialcontenttransform;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

class ContentMapping {

  private final String bundlePath;
  private final String contentPath;
  private final Set<String> ignoreImportProviders;

  ContentMapping(String bundlePath, String contentPath, String ignoreImportProviders) {
    this.bundlePath = bundlePath;
    this.contentPath = contentPath;
    this.ignoreImportProviders = new HashSet<>(Arrays.asList(StringUtils.split(ignoreImportProviders, ",")));
  }

  public String getBundlePath() {
    return this.bundlePath;
  }

  public String getContentPath() {
    return this.contentPath;
  }

  public boolean isJson() {
    return !ignoreImportProviders.contains("json");
  }

  public boolean isXml() {
    return !ignoreImportProviders.contains("xml");
  }

}
