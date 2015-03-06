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
package io.wcm.maven.plugins.contentpackage.unpacker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.sun.org.apache.xml.internal.serialize.LineSeparator;

/**
 * Manages unpacking ZIP file content applying exclude patterns.
 */
public class ContentUnpacker {

  private final Pattern[] excludeFiles;
  private final Pattern[] excludeNodes;
  private final Pattern[] excludeProperties;

  /**
   * @param excludeFiles Exclude files
   * @param excludeNodes Exclude nodes
   * @param excludeProperties Exclude properties
   * @throws MojoFailureException
   */
  public ContentUnpacker(String[] excludeFiles, String[] excludeNodes, String[] excludeProperties) throws MojoFailureException {
    this.excludeFiles = toPatternArray(excludeFiles);
    this.excludeNodes = toPatternArray(excludeNodes);
    this.excludeProperties = toPatternArray(excludeProperties);
  }

  private static Pattern[] toPatternArray(String[] patternStrings) throws MojoFailureException {
    if (patternStrings == null) {
      return new Pattern[0];
    }
    Pattern[] patterns = new Pattern[patternStrings.length];
    for (int i = 0; i < patternStrings.length; i++) {
      try {
        patterns[i] = Pattern.compile(patternStrings[i]);
      }
      catch (PatternSyntaxException ex) {
        throw new MojoFailureException("Invalid regexp pattern: " + patternStrings[i], ex);
      }
    }
    return patterns;
  }

  private static boolean exclude(String name, Pattern[] patterns) {
    for (Pattern pattern : patterns) {
      if (pattern.matcher(name).matches()) {
        return true;
      }
    }
    return false;
  }

  private boolean applyXmlExcludes(String name) {
    if (this.excludeNodes.length == 0 & this.excludeProperties.length == 0) {
      return false;
    }
    return StringUtils.endsWith(name, "/.content.xml");
  }

  /**
   * Unpacks file
   * @param file File
   * @param outputDirectory Output directory
   * @throws MojoExecutionException
   */
  public void unpack(File file, File outputDirectory) throws MojoExecutionException {
    ZipFile zipFile = null;
    try {
      zipFile = new ZipFile(file);
      Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
      while (entries.hasMoreElements()) {
        ZipArchiveEntry entry = entries.nextElement();
        if (!exclude(entry.getName(), excludeFiles)) {
          unpackEntry(zipFile, entry, outputDirectory);
        }
      }
    }
    catch (IOException ex) {
      throw new MojoExecutionException("Error reading content package " + file.getAbsolutePath(), ex);
    }
    finally {
      IOUtils.closeQuietly(zipFile);
    }
  }

  private void unpackEntry(ZipFile zipFile, ZipArchiveEntry entry, File outputDirectory)
      throws IOException, MojoExecutionException {
    if (entry.isDirectory()) {
      File directory = FileUtils.getFile(outputDirectory, entry.getName());
      directory.mkdirs();
    }
    else {
      InputStream entryStream = null;
      FileOutputStream fos = null;
      try {
        entryStream = zipFile.getInputStream(entry);
        File outputFile = FileUtils.getFile(outputDirectory, entry.getName());
        if (outputFile.exists()) {
          outputFile.delete();
        }
        File directory = outputFile.getParentFile();
        directory.mkdirs();
        fos = new FileOutputStream(outputFile);
        if (applyXmlExcludes(entry.getName())) {
          // write file with XML filtering
          try {
            writeXmlWithExcludes(entryStream, fos);
          }
          catch (JDOMException ex) {
            throw new MojoExecutionException("Unable to parse XML file: " + entry.getName(), ex);
          }
        }
        else {
          // write file directly without XML filtering
          IOUtils.copy(entryStream, fos);
        }
      }
      finally {
        IOUtils.closeQuietly(entryStream);
        IOUtils.closeQuietly(fos);
      }
    }
  }

  private void writeXmlWithExcludes(InputStream inputStream, OutputStream outputStream)
      throws IOException, JDOMException {
    SAXBuilder saxBuilder = new SAXBuilder();
    Document doc = saxBuilder.build(inputStream);
    applyXmlExcludes(doc.getRootElement(), "");

    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat()
        .setLineSeparator(LineSeparator.Unix));
    outputter.setXMLOutputProcessor(new OneAttributePerLineXmlProcessor());
    outputter.output(doc, outputStream);
    outputStream.flush();
  }

  private void applyXmlExcludes(Element element, String parentPath) {
    String path = parentPath + "/" + element.getName();
    if (exclude(path, this.excludeNodes)) {
      element.detach();
      return;
    }
    List<Attribute> attributes = new ArrayList<>(element.getAttributes());
    for (Attribute attribute : attributes) {
      if (exclude(attribute.getQualifiedName(), this.excludeProperties)) {
        attribute.detach();
      }
    }
    List<Element> children = new ArrayList<>(element.getChildren());
    for (Element child : children) {
      applyXmlExcludes(child, path);
    }
  }

}
