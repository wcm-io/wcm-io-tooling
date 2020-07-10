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

import static io.wcm.tooling.commons.contentpackagebuilder.NameUtil.ensureValidPath;
import static org.apache.jackrabbit.vault.util.Constants.CONFIG_XML;
import static org.apache.jackrabbit.vault.util.Constants.DOT_CONTENT_XML;
import static org.apache.jackrabbit.vault.util.Constants.FILTER_XML;
import static org.apache.jackrabbit.vault.util.Constants.META_DIR;
import static org.apache.jackrabbit.vault.util.Constants.PACKAGE_DEFINITION_XML;
import static org.apache.jackrabbit.vault.util.Constants.PROPERTIES_XML;
import static org.apache.jackrabbit.vault.util.Constants.ROOT_DIR;
import static org.apache.jackrabbit.vault.util.Constants.SETTINGS_XML;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.vault.packaging.PackageProperties;
import org.apache.jackrabbit.vault.util.PlatformNameFormat;
import org.w3c.dom.Document;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;

import io.wcm.tooling.commons.contentpackagebuilder.ContentFolderSplitter.ContentPart;
import io.wcm.tooling.commons.contentpackagebuilder.element.ContentElement;

/**
 * Represents an AEM content package.
 * Content like structured JCR data and binary files can be added.
 * This class is not thread-safe.
 */
public final class ContentPackage implements Closeable {

  private final PackageMetadata metadata;
  private final ZipOutputStream zip;
  private final Transformer transformer;
  private final XmlContentBuilder xmlContentBuilder;

  private static final String CONTENT_TYPE_CHARSET_EXTENSION = ";charset=";
  private static final String DOT_DIR_FOLDER = ".dir";

  /**
   * @param os Output stream
   */
  ContentPackage(PackageMetadata metadata, OutputStream os) throws IOException {
    this.metadata = metadata;
    this.zip = new ZipOutputStream(os);

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    try {
      transformerFactory.setAttribute("indent-number", 2);
    }
    catch (IllegalArgumentException ex) {
      // Implementation does not support configuration property. Ignore.
    }
    try {
      this.transformer = transformerFactory.newTransformer();
      try {
        this.transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      }
      catch (IllegalArgumentException ex) {
        // Implementation does not support output property. Ignore.
      }
      try {
        this.transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      }
      catch (IllegalArgumentException ex) {
        // Implementation does not support output property. Ignore.
      }
    }
    catch (TransformerException ex) {
      throw new RuntimeException("Failed to set up XML transformer: " + ex.getMessage(), ex);
    }

    this.xmlContentBuilder = new XmlContentBuilder(metadata.getXmlNamespaces());

    buildPackageMetadata();
  }

  /**
   * Adds a page with given content. The "cq:Page/cq:PageContent envelope" is added automatically.
   * @param path Full content path of page.
   * @param content Hierarchy of content elements.
   * @throws IOException I/O exception
   */
  public void addPage(String path, ContentElement content) throws IOException {
    String fullPath = buildJcrPathForZip(path) + "/" + DOT_CONTENT_XML;
    Document doc = xmlContentBuilder.buildPage(content);
    writeXmlDocument(fullPath, doc);
  }

  /**
   * Adds a page with given content. The "cq:Page/cq:PageContent envelope" is added automatically.
   * @param path Full content path of page.
   * @param content Map with page properties. If the map contains nested maps this builds a tree of JCR nodes.
   *          The key of the nested map in its parent map is the node name,
   *          the nested map contain the properties of the child node.
   * @throws IOException I/O exception
   */
  public void addPage(String path, Map<String, Object> content) throws IOException {
    String fullPath = buildJcrPathForZip(path) + "/" + DOT_CONTENT_XML;
    Document doc = xmlContentBuilder.buildPage(content);
    writeXmlDocument(fullPath, doc);
  }

  /**
   * Add some JCR content structure directly to the package.
   * @param path Full content path of content root node.
   * @param content Hierarchy of content elements.
   * @throws IOException I/O exception
   */
  public void addContent(String path, ContentElement content) throws IOException {
    String basePath = buildJcrPathForZip(path);
    List<ContentPart> parts = ContentFolderSplitter.split(ContentElementConverter.toMap(content));
    for (ContentPart part : parts) {
      String fullPath = basePath + part.getPath() + "/" + DOT_CONTENT_XML;
      Document doc = xmlContentBuilder.buildContent(part.getContent());
      writeXmlDocument(fullPath, doc);
    }
  }

  /**
   * Add some JCR content structure directly to the package.
   * @param path Full content path of content root node.
   * @param content Map with node properties. If the map contains nested maps this builds a tree of JCR nodes.
   *          The key of the nested map in its parent map is the node name,
   *          the nested map contain the properties of the child node.
   * @throws IOException I/O exception
   */
  public void addContent(String path, Map<String, Object> content) throws IOException {
    String basePath = buildJcrPathForZip(path);
    List<ContentPart> parts = ContentFolderSplitter.split(content);
    for (ContentPart part : parts) {
      String fullPath = basePath + part.getPath() + "/" + DOT_CONTENT_XML;
      Document doc = xmlContentBuilder.buildContent(part.getContent());
      writeXmlDocument(fullPath, doc);
    }
  }

  /**
   * Adds a binary file.
   * @param path Full content path and file name of file
   * @param inputStream Input stream with binary dta
   * @throws IOException I/O exception
   */
  public void addFile(String path, InputStream inputStream) throws IOException {
    addFile(path, inputStream, null);
  }

  /**
   * Adds a binary file with explicit mime type.
   * @param path Full content path and file name of file
   * @param inputStream Input stream with binary data
   * @param contentType Mime type, optionally with ";charset=XYZ" extension
   * @throws IOException I/O exception
   */
  public void addFile(String path, InputStream inputStream, String contentType) throws IOException {
    String fullPath = buildJcrPathForZip(path);
    writeBinaryFile(fullPath, inputStream);

    if (StringUtils.isNotEmpty(contentType)) {
      String mimeType = StringUtils.substringBefore(contentType, CONTENT_TYPE_CHARSET_EXTENSION);
      String encoding = StringUtils.substringAfter(contentType, CONTENT_TYPE_CHARSET_EXTENSION);

      String fullPathMetadata = fullPath + DOT_DIR_FOLDER + "/" + DOT_CONTENT_XML;
      Document doc = xmlContentBuilder.buildNtFile(mimeType, encoding);
      writeXmlDocument(fullPathMetadata, doc);
    }
  }

  /**
   * If path parts contain namespace definitions they need to be escaped for the ZIP file.
   * Example: oak:index -> jcr_root/_oak_index
   * @param path Path
   * @return Safe path
   */
  @VisibleForTesting
  @SuppressWarnings("PMD.UseStringBufferForStringAppends")
  static String buildJcrPathForZip(final String path) {
    String normalizedPath = StringUtils.defaultString(path);
    if (!normalizedPath.startsWith("/")) {
      normalizedPath = "/" + normalizedPath;
    }
    ensureValidPath(path);
    return ROOT_DIR + PlatformNameFormat.getPlatformPath(normalizedPath);
  }

  /**
   * Adds a binary file.
   * @param path Full content path and file name of file
   * @param file File with binary data
   * @throws IOException I/O exception
   */
  public void addFile(String path, File file) throws IOException {
    addFile(path, file, null);
  }

  /**
   * Adds a binary file with explicit mime type.
   * @param path Full content path and file name of file
   * @param file File with binary data
   * @param contentType Mime type, optionally with ";charset=XYZ" extension
   * @throws IOException I/O exception
   */
  public void addFile(String path, File file, String contentType) throws IOException {
    try (InputStream is = new FileInputStream(file)) {
      addFile(path, is, contentType);
    }
  }

  /**
   * Close the underlying ZIP stream of the package.
   * @throws IOException I/O exception
   */
  @Override
  public void close() throws IOException {
    zip.flush();
    zip.close();
  }

  /**
   * Get root path of the package. This does only work if there is only one filter of the package.
   * If they are more filters use {@link #getFilters()} instead.
   * @return Root path of package
   */
  public String getRootPath() {
    if (metadata.getFilters().size() == 1) {
      return metadata.getFilters().get(0).getRootPath();
    }
    else {
      throw new IllegalStateException("Content package has more than one package filter - please use getFilters().");
    }
  }

  /**
   * Get filters defined for this package.
   * @return List of package filters, optionally with include/exclude rules.
   */
  public List<PackageFilter> getFilters() {
    return metadata.getFilters();
  }

  /**
   * Build all package metadata files based on templates.
   * @throws IOException I/O exception
   */
  private void buildPackageMetadata() throws IOException {
    metadata.validate();
    buildTemplatedMetadataFile(META_DIR + "/" + CONFIG_XML);
    buildPropertiesFile(META_DIR + "/" + PROPERTIES_XML);
    buildTemplatedMetadataFile(META_DIR + "/" + SETTINGS_XML);
    buildTemplatedMetadataFile(META_DIR + "/" + PACKAGE_DEFINITION_XML);
    writeXmlDocument(META_DIR + "/" + FILTER_XML, xmlContentBuilder.buildFilter(metadata.getFilters()));

    // package thumbnail
    byte[] thumbnailImage = metadata.getThumbnailImage();
    if (thumbnailImage != null) {
      zip.putNextEntry(new ZipEntry(META_DIR + "/definition/thumbnail.png"));
      try {
        zip.write(thumbnailImage);
      }
      finally {
        zip.closeEntry();
      }
    }
  }

  /**
   * Read template file from classpath, replace variables and store it in the zip stream.
   * @param path Path
   * @throws IOException I/O exception
   */
  private void buildTemplatedMetadataFile(String path) throws IOException {
    try (InputStream is = getClass().getResourceAsStream("/content-package-template/" + path)) {
      String xmlContent = IOUtils.toString(is, StandardCharsets.UTF_8);
      for (Map.Entry<String, Object> entry : metadata.getVars().entrySet()) {
        xmlContent = StringUtils.replace(xmlContent, "{{" + entry.getKey() + "}}",
            StringEscapeUtils.escapeXml10(entry.getValue().toString()));
      }
      zip.putNextEntry(new ZipEntry(path));
      try {
        zip.write(xmlContent.getBytes(Charsets.UTF_8));
      }
      finally {
        zip.closeEntry();
      }
    }
  }

  /**
   * Build java Properties XML file.
   * @param path Path
   * @throws IOException I/O exception
   */
  private void buildPropertiesFile(String path) throws IOException {
    Properties properties = new Properties();
    properties.put(PackageProperties.NAME_REQUIRES_ROOT, Boolean.toString(false));
    properties.put("allowIndexDefinitions", Boolean.toString(false));

    for (Map.Entry<String, Object> entry : metadata.getVars().entrySet()) {
      String value = Objects.toString(entry.getValue());
      if (StringUtils.isNotEmpty(value)) {
        properties.put(entry.getKey(), value);
      }
    }

    zip.putNextEntry(new ZipEntry(path));
    try {
      properties.storeToXML(zip, null);
    }
    finally {
      zip.closeEntry();
    }
  }

  /**
   * Writes an XML document as binary file entry to the ZIP output stream.
   * @param path Content path
   * @param doc XML content
   * @throws IOException I/O exception
   */
  private void writeXmlDocument(String path, Document doc) throws IOException {
    zip.putNextEntry(new ZipEntry(path));
    try {
      DOMSource source = new DOMSource(doc);
      StreamResult result = new StreamResult(zip);
      transformer.transform(source, result);
    }
    catch (TransformerException ex) {
      throw new IOException("Failed to generate XML: " + ex.getMessage(), ex);
    }
    finally {
      zip.closeEntry();
    }
  }

  /**
   * Writes an binary file entry to the ZIP output stream.
   * @param path Content path
   * @param is Input stream with binary data
   * @throws IOException I/O exception
   */
  private void writeBinaryFile(String path, InputStream is) throws IOException {
    zip.putNextEntry(new ZipEntry(path));
    try {
      IOUtils.copy(is, zip);
    }
    finally {
      zip.closeEntry();
    }
  }

}
