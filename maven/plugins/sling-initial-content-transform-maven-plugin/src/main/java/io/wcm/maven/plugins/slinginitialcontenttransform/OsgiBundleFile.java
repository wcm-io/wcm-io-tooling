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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.commons.osgi.ManifestHeader;

class OsgiBundleFile implements Closeable {

  static final String HEADER_INITIAL_CONTENT = "Sling-Initial-Content";
  static final String HEADER_NAMESPACES = "Sling-Namespaces";

  private final JarFile jarFile;
  private final List<ContentMapping> contentMappings;
  private final Map<String, String> namespaces;

  OsgiBundleFile(File file) throws IOException {
    this.jarFile = new JarFile(file);
    this.contentMappings = buildContentMappings(jarFile);
    this.namespaces = buildNamespaces(jarFile);
  }

  /**
   * Reads mappings from bundle paths to content paths defined in Sling-Initial-Content manifest entry.
   * @param jarFile JAR file
   * @return Content mappings
   * @throws IOException I/O exception
   */
  private static List<ContentMapping> buildContentMappings(JarFile jarFile) throws IOException {
    List<ContentMapping> result = new ArrayList<>();
    String manifestAttribute = getManifestAttribute(jarFile, HEADER_INITIAL_CONTENT);
    ManifestHeader header = null;
    if (manifestAttribute != null) {
      header = ManifestHeader.parse(manifestAttribute);
    }
    if (header != null) {
      for (ManifestHeader.Entry entry : header.getEntries()) {
        String bundlePath = entry.getValue();
        String contentPath = entry.getDirectiveValue("path");
        String ignoreImportProviders = entry.getDirectiveValue("ignoreImportProviders");
        if (StringUtils.isNoneBlank(bundlePath, contentPath)) {
          result.add(new ContentMapping(bundlePath, contentPath, ignoreImportProviders));
        }
      }
    }
    return result;
  }

  /**
   * Builds map with Sling namespace definitions.
   * @param jarFile JAR file
   * @return Namespaces
   * @throws IOException I/O exception
   */
  private static Map<String, String> buildNamespaces(JarFile jarFile) throws IOException {
    Map<String, String> result = new HashMap<>();
    String manifestAttribute = getManifestAttribute(jarFile, HEADER_NAMESPACES);
    if (manifestAttribute != null) {
      String[] entries = StringUtils.split(manifestAttribute, ",");
      for (String mapping : entries) {
        String key = StringUtils.substringBefore(mapping, "=");
        String value = StringUtils.substringAfter(mapping, "=");
        if (StringUtils.isNoneBlank(key, value)) {
          result.put(key, value);
        }
      }
    }
    return result;
  }

  private static String getManifestAttribute(JarFile jarFile, String headerName) throws IOException {
    Manifest manifest = jarFile.getManifest();
    if (manifest != null) {
      return manifest.getMainAttributes().getValue(headerName);
    }
    return null;
  }

  /**
   * @return true if bundle has any Sling-Initial-Content
   */
  public boolean hasContent() {
    return !contentMappings.isEmpty();
  }

  /**
   * @return Returns all configured Sling-Initial-Content mappings.
   */
  public List<ContentMapping> getContentMappings() {
    return contentMappings;
  }

  /**
   * @return Returns sling namespaces.
   */
  public Map<String, String> getNamespaces() {
    return this.namespaces;
  }

  /**
   * Get all Sling-Initial-Content entries matching for the given mapping.
   * @param mapping Content mapping
   * @return Content entries. Contains no directory entries.
   */
  @SuppressWarnings("null")
  public Stream<BundleEntry> getEntries(ContentMapping mapping) {
    Pattern bundlePathPattern = Pattern.compile("^" + Pattern.quote(mapping.getBundlePath()) + "/.*$");
    return jarFile.stream()
        .filter(entry -> !entry.isDirectory())
        .filter(entry -> bundlePathPattern.matcher(entry.getName()).matches())
        .map(entry -> {
          String path = mapping.getContentPath() + StringUtils.substringAfter(entry.getName(), mapping.getBundlePath());
          return new BundleEntry(path, jarFile, entry);
        });
  }

  /**
   * Get all JAR entries not matching any Sling-Initial-Content mapped path.
   * @return JAR entries
   */
  @SuppressWarnings("null")
  public Stream<BundleEntry> getNonContentEntries() {
    Pattern allBundlePathsPattern = Pattern.compile("^(" + contentMappings.stream()
        .map(ContentMapping::getBundlePath)
        .map(Pattern::quote)
        .collect(Collectors.joining("|")) + ")(/.*)?$");
    return jarFile.stream()
        .filter(entry -> !allBundlePathsPattern.matcher(entry.getName()).matches())
        .map(entry -> new BundleEntry(entry.getName(), jarFile, entry));
  }

  @Override
  public void close() throws IOException {
    jarFile.close();
  }

}
