/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2015 wcm.io
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * Builds a {@link ContentPackage} instance with metadata.
 * This class is not thread-safe.
 */
public final class ContentPackageBuilder {

  private final PackageMetadata metadata = new PackageMetadata();

  /**
   * Set content package name.
   * @param value Package name
   * @return this
   */
  public ContentPackageBuilder name(String value) {
    metadata.setName(value);
    return this;
  }

  /**
   * Set content package group.
   * @param value Package group
   * @return this
   */
  public ContentPackageBuilder group(String value) {
    metadata.setGroup(value);
    return this;
  }

  /**
   * Set content package description.
   * @param value Package description
   * @return this
   */
  public ContentPackageBuilder description(String value) {
    metadata.setDescription(value);
    return this;
  }

  /**
   * Set use name who created the package.
   * @param value Created by user name (default: 'admin')
   * @return this
   */
  public ContentPackageBuilder createdBy(String value) {
    metadata.setCreatedBy(value);
    return this;
  }

  /**
   * Set timestamp for package creation.
   * @param value Creation timestamp (default: now)
   * @return this
   */
  public ContentPackageBuilder created(Date value) {
    metadata.setCreated(value);
    return this;
  }

  /**
   * Set package version.
   * @param value Package version
   * @return this
   */
  public ContentPackageBuilder version(String value) {
    metadata.setVersion(value);
    return this;
  }

  /**
   * Creates a package filter with this root path.
   * This implicitly adds a {@link PackageFilter} with this pah and no further rules.
   * If this is executed multiple times multiple filters are addded.
   * @param value Root path for package
   * @return this
   */
  public ContentPackageBuilder rootPath(String value) {
    metadata.addFilter(new PackageFilter(value));
    return this;
  }

  /**
   * Add package filter.
   * If this is executed multiple times multiple filters are added.
   * @param value Package filter optionally with include/exclude rules.
   * @return this
   */
  public ContentPackageBuilder filter(PackageFilter value) {
    metadata.addFilter(value);
    return this;
  }

  /**
   * Register a XML namespace that is used by your content added to the JCR XML.
   * This method can be called multiple times to register multiple namespaces.
   * The JCR namespaces "jcr", "nt", "cq" and "sling" are registered by default.
   * @param prefix Namespace prefix
   * @param uri Namespace URI
   * @return this
   */
  public ContentPackageBuilder xmlNamespace(String prefix, String uri) {
    metadata.addXmlNamespace(prefix, uri);
    return this;
  }

  /**
   * Build {@link ContentPackage} to which additional content (Pages or binary files) can be added.
   * Please make sure you call the {@link ContentPackage#close()} method when all content was added.
   * @param outputStream Output stream
   * @return Content package
   * @throws IOException
   */
  public ContentPackage build(OutputStream outputStream) throws IOException {
    return new ContentPackage(metadata, outputStream);
  }

  /**
   * Build {@link ContentPackage} to which additional content (Pages or binary files) can be added.
   * Please make sure you call the {@link ContentPackage#close()} method when all content was added.
   * @param file Output file
   * @return Content package
   * @throws IOException
   */
  public ContentPackage build(File file) throws IOException {
    return build(new FileOutputStream(file));
  }

}
