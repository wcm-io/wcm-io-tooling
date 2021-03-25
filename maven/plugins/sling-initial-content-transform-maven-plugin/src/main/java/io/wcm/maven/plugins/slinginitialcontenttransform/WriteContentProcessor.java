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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.jackrabbit.JcrConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.maven.plugins.slinginitialcontenttransform.contentparser.JsonContentLoader;
import io.wcm.maven.plugins.slinginitialcontenttransform.contentparser.XmlContentLoader;
import io.wcm.tooling.commons.contentpackagebuilder.ContentPackage;
import io.wcm.tooling.commons.contentpackagebuilder.element.ContentElement;

/**
 * Actually writes the content to the content package.
 */
class WriteContentProcessor implements BundleEntryProcessor {

  private final Set<String> ignoreFolderPaths;
  private final JsonContentLoader jsonContentLoader = new JsonContentLoader();
  private final XmlContentLoader xmlContentLoader = new XmlContentLoader();

  private static final Logger log = LoggerFactory.getLogger(WriteContentProcessor.class);

  WriteContentProcessor(Set<String> ignoreFolderPaths) {
    this.ignoreFolderPaths = ignoreFolderPaths;
  }

  @Override
  public void directory(String path, ContentPackage contentPackage) throws IOException {
    if (ignoreFolderPaths.contains(path)) {
      return;
    }
    log.debug("Add folder: {}", path);
    Map<String, Object> props = new HashMap<>();
    props.put(JcrConstants.JCR_PRIMARYTYPE, "sling:Folder");
    contentPackage.addContent(path, props);
  }

  @Override
  public void jsonContent(String path, BundleEntry entry, ContentPackage contentPackage) throws IOException {
    log.debug("Add JSON content: {}", path);
    try (InputStream is = entry.getInputStream()) {
      ContentElement contentElement = jsonContentLoader.load(is);
      contentPackage.addContent(path, contentElement);
    }
  }

  @Override
  public void xmlContent(String path, BundleEntry entry, ContentPackage contentPackage) throws IOException {
    log.debug("Add XML content: {}", path);
    try (InputStream is = entry.getInputStream()) {
      ContentElement contentElement = xmlContentLoader.load(is);
      // we assume xml content is only used in cases where additional properties should be set for a binary file
      contentPackage.addContentForFile(path, contentElement);
    }
  }

  @Override
  public void binaryContent(String path, BundleEntry entry, ContentPackage contentPackage) throws IOException {
    log.debug("Add binary content: {}", path);
    try (InputStream is = entry.getInputStream()) {
      contentPackage.addFile(path, is);
    }
  }

}
