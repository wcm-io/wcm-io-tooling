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

import java.util.HashSet;
import java.util.Set;

import io.wcm.tooling.commons.contentpackagebuilder.ContentPackage;

/**
 * Collects all non-directory paths with generated content.
 */
public class CollectNonDirectoryPathsProcessor implements BundleEntryProcessor {

  private final Set<String> paths = new HashSet<>();

  @Override
  public void directory(String path, ContentPackage contentPackage) {
    // ignore
  }

  @Override
  public void jsonContent(String path, BundleEntry entry, ContentPackage contentPackage) {
    paths.add(path);
  }

  @Override
  public void xmlContent(String path, BundleEntry entry, ContentPackage contentPackage) {
    paths.add(path);
  }

  @Override
  public void binaryContent(String path, BundleEntry entry, ContentPackage contentPackage) {
    paths.add(path);
  }

  public Set<String> getPaths() {
    return this.paths;
  }

}
