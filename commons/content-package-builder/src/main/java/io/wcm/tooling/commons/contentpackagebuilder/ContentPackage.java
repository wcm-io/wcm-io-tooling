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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import com.google.common.base.Charsets;

/**
 * Builds CMS content packages.
 * This class is not thread-safe.
 */
public class ContentPackage {

  private final PackageMetadata metadata;
  private final ZipOutputStream zip;
  private final TransformerFactory transformerFactory;
  private final PageXmlBuilder pageXmlBuilder = new PageXmlBuilder();

  /**
   * @param os Output stream
   * @throws IOException
   */
  ContentPackage(PackageMetadata metadata, OutputStream os) throws IOException {
    this.metadata = metadata;
    this.zip = new ZipOutputStream(os);
    this.transformerFactory = TransformerFactory.newInstance();
    buildPackageMetadata();
  }

  /**
   * Create page with given content.
   * @param path Full content path of page.
   * @param content Map with page properties (my contain nested maps for subnodes)
   * @throws IOException
   */
  public void addPage(String path, Map<String, Object> content) throws IOException {
    String fullPath = "jcr_root" + path + "/.content.xml";
    Document doc = pageXmlBuilder.build(content);
    writeXmlDocument(fullPath, doc);
  }

  /**
   * Create a binary file.
   * @param path Full content path and file name of file
   * @param inputStream Input stream with binary dta
   * @throws IOException
   */
  public void addFile(String path, InputStream inputStream) throws IOException {
    String fullPath = "jcr_root" + path;
    writeBinaryFile(fullPath, inputStream);
  }

  /**
   * Create a binary file.
   * @param path Full content path and file name of file
   * @param data Byte array with binary data
   * @throws IOException
   */
  public void addFile(String path, byte[] data) throws IOException {
    try (InputStream is = new ByteArrayInputStream(data)) {
      addFile(path, is);
    }
  }

  /**
   * Create a binary file.
   * @param path Full content path and file name of file
   * @param file File with binary data
   * @throws IOException
   */
  public void addFile(String path, File file) throws IOException {
    try (InputStream is = new FileInputStream(file)) {
      addFile(path, is);
    }
  }

  /**
   * Close ZIP stream
   * @throws IOException
   */
  public void close() throws IOException {
    zip.flush();
    zip.close();
  }

  /**
   * Build all package metadata files based on templates.
   * @throws IOException
   */
  private void buildPackageMetadata() throws IOException {
    metadata.validate();
    buildPackageMetadataFile("META-INF/vault/config.xml");
    buildPackageMetadataFile("META-INF/vault/filter.xml");
    buildPackageMetadataFile("META-INF/vault/properties.xml");
    buildPackageMetadataFile("META-INF/vault/settings.xml");
  }

  /**
   * Read template file from classpath, replace variables and store it in the zip stream.
   * @param path Path
   * @throws IOException
   */
  private void buildPackageMetadataFile(String path) throws IOException {
    try (InputStream is = getClass().getResourceAsStream("/content-package-template/" + path)) {
      String xmlContent = IOUtils.toString(is);
      for (Map.Entry<String, Object> entry : metadata.getVars(pageXmlBuilder.getJcrTimestampFormat()).entrySet()) {
        xmlContent = StringUtils.replace(xmlContent, "{{" + entry.getKey() + "}}",
            StringEscapeUtils.escapeXml(entry.getValue().toString()));
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
   * Writes an XML document as binary file entry to the ZIP output stream.
   * @param path Content path
   * @param doc XML conent
   * @throws IOException
   */
  private void writeXmlDocument(String path, Document doc) throws IOException {
    zip.putNextEntry(new ZipEntry(path));
    try {
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(doc);
      StreamResult result = new StreamResult(zip);
      transformer.transform(source, result);
    }
    catch (TransformerException ex) {
      throw new IOException("Faild to generate XML: " + ex.getMessage(), ex);
    }
    finally {
      zip.closeEntry();
    }
  }

  /**
   * Writes an binary file entry to the ZIP output stream.
   * @param path Content path
   * @param is Input stream with binary data
   * @throws IOException
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
