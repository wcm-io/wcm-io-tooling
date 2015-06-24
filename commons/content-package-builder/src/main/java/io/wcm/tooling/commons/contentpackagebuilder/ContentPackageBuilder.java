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
   * @param value Package name
   * @return this
   */
  public ContentPackageBuilder name(String value) {
    metadata.setName(value);
    return this;
  }

  /**
   * @param value Package group
   * @return this
   */
  public ContentPackageBuilder group(String value) {
    metadata.setGroup(value);
    return this;
  }

  /**
   * @param value Package description
   * @return this
   */
  public ContentPackageBuilder description(String value) {
    metadata.setDescription(value);
    return this;
  }

  /**
   * @param value Created by user name (default: 'admin')
   * @return this
   */
  public ContentPackageBuilder createdBy(String value) {
    metadata.setCreatedBy(value);
    return this;
  }

  /**
   * @param value Creation timestamp (default: now)
   * @return this
   */
  public ContentPackageBuilder created(Date value) {
    metadata.setCreated(value);
    return this;
  }

  /**
   * @param value Package version
   * @return this
   */
  public ContentPackageBuilder version(String value) {
    metadata.setVersion(value);
    return this;
  }

  /**
   * Simplified version for setting up package filter - creates a page with one filter containing this path.
   * @param value Root path for package
   * @return this
   */
  public ContentPackageBuilder rootPath(String value) {
    metadata.setRootPath(value);
    return this;
  }

  /**
   * Register an XML namespace that is reuqired in the JCR XML.
   * By default the JCR namespaces "jcr", "nt", "cq" and "sling" are registered.
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
