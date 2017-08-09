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
package io.wcm.tooling.commons.packmgr.unpack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.jcr.Binary;
import javax.jcr.Item;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.vault.util.DocViewProperty;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.LineSeparator;
import org.jdom2.output.XMLOutputter;

import io.wcm.tooling.commons.packmgr.PackageManagerException;

/**
 * Manages unpacking ZIP file content applying exclude patterns.
 */
public final class ContentUnpacker {

  private static final String MIXINS_PROPERTY = "jcr:mixinTypes";

  private final Pattern[] excludeFiles;
  private final Pattern[] excludeNodes;
  private final Pattern[] excludeProperties;
  private final Pattern[] excludeMixins;

  /**
   * @param properties Configuration properties
   */
  public ContentUnpacker(ContentUnpackerProperties properties) {
    this.excludeFiles = toPatternArray(properties.getExcludeFiles());
    this.excludeNodes = toPatternArray(properties.getExcludeNodes());
    this.excludeProperties = toPatternArray(properties.getExcludeProperties());
    this.excludeMixins = toPatternArray(properties.getExcludeMixins());
  }

  private static Pattern[] toPatternArray(String[] patternStrings) {
    if (patternStrings == null) {
      return new Pattern[0];
    }
    Pattern[] patterns = new Pattern[patternStrings.length];
    for (int i = 0; i < patternStrings.length; i++) {
      try {
        patterns[i] = Pattern.compile(patternStrings[i]);
      }
      catch (PatternSyntaxException ex) {
        throw new PackageManagerException("Invalid regexp pattern: " + patternStrings[i], ex);
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
   */
  public void unpack(File file, File outputDirectory) {
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
      throw new PackageManagerException("Error reading content package " + file.getAbsolutePath(), ex);
    }
    finally {
      IOUtils.closeQuietly(zipFile);
    }
  }

  private void unpackEntry(ZipFile zipFile, ZipArchiveEntry entry, File outputDirectory) throws IOException {
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
            throw new PackageManagerException("Unable to parse XML file: " + entry.getName(), ex);
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
        .setIndent("    ")
        .setLineSeparator(LineSeparator.UNIX));
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
      else if (StringUtils.equals(attribute.getQualifiedName(), MIXINS_PROPERTY)) {
        String filteredValue = filterMixinsPropertyValue(attribute.getValue());
        if (StringUtils.isBlank(filteredValue)) {
          attribute.detach();
        }
        else {
          attribute.setValue(filteredValue);
        }
      }
    }
    List<Element> children = new ArrayList<>(element.getChildren());
    for (Element child : children) {
      applyXmlExcludes(child, path);
    }
  }

  private String filterMixinsPropertyValue(String value) {
    if (this.excludeMixins.length == 0 || StringUtils.isBlank(value)) {
      return value;
    }

    DocViewProperty prop = DocViewProperty.parse(MIXINS_PROPERTY, value);
    List<Value> mixins = new ArrayList<>();
    for (int i = 0; i < prop.values.length; i++) {
      String mixin = prop.values[i];
      if (!exclude(mixin, this.excludeMixins)) {
        mixins.add(new MockValue(mixin, PropertyType.STRING));
      }
    }

    if (mixins.isEmpty()) {
      return null;
    }

    try {
      return DocViewProperty.format(new MockProperty(MIXINS_PROPERTY, true, mixins.toArray(new Value[mixins.size()])));
    }
    catch (RepositoryException ex) {
      throw new RuntimeException("Unable to format value for " + MIXINS_PROPERTY, ex);
    }
  }


  /**
   * Mock implementations of JCR property and value to be handed over to {@link DocViewProperty#format(Property)}
   * method.
   */
  private static class MockProperty implements Property, PropertyDefinition {

    private final String name;
    private final boolean multiple;
    private final Value[] values;

    MockProperty(String name, boolean multiple, Value[] values) {
      this.name = name;
      this.multiple = multiple;
      this.values = values;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public int getType() {
      if (values.length > 0) {
        return values[0].getType();
      }
      return PropertyType.UNDEFINED;
    }

    @Override
    public boolean isMultiple() {
      return multiple;
    }

    @Override
    public Value getValue() throws ValueFormatException {
      if (multiple) {
        throw new ValueFormatException("Property is multiple.");
      }
      return values[0];
    }

    @Override
    public Value[] getValues() throws ValueFormatException {
      if (!multiple) {
        throw new ValueFormatException("Property is not multiple.");
      }
      return values;
    }

    @Override
    public PropertyDefinition getDefinition() {
      return this;
    }


    // -- unsupported methods --

    @Override
    public String getPath() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Item getAncestor(int depth) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Node getParent() {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getDepth() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Session getSession() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNode() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNew() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isModified() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSame(Item otherItem) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void accept(ItemVisitor visitor) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void save() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void refresh(boolean keepChanges) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(Value value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(Value[] value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(String value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(String[] value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(InputStream value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(Binary value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(long value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(double value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(BigDecimal value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(Calendar value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(boolean value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(Node value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getString() {
      throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getStream() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Binary getBinary() {
      throw new UnsupportedOperationException();
    }

    @Override
    public long getLong() {
      throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble() {
      throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal getDecimal() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Calendar getDate() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBoolean() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Node getNode() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Property getProperty() {
      throw new UnsupportedOperationException();
    }

    @Override
    public long getLength() {
      throw new UnsupportedOperationException();
    }

    @Override
    public long[] getLengths() {
      throw new UnsupportedOperationException();
    }

    @Override
    public NodeType getDeclaringNodeType() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAutoCreated() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMandatory() {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getOnParentVersion() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isProtected() {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getRequiredType() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String[] getValueConstraints() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Value[] getDefaultValues() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String[] getAvailableQueryOperators() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFullTextSearchable() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isQueryOrderable() {
      throw new UnsupportedOperationException();
    }

  }

  private static class MockValue implements Value {

    private final String value;
    private final int type;

    MockValue(String value, int type) {
      this.value = value;
      this.type = type;
    }

    @Override
    public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
      return value;
    }

    @Override
    public int getType() {
      return type;
    }


    // -- unsupported methods --

    @Override
    public InputStream getStream() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Binary getBinary() {
      throw new UnsupportedOperationException();
    }

    @Override
    public long getLong() {
      throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble() {
      throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal getDecimal() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Calendar getDate() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBoolean() {
      throw new UnsupportedOperationException();
    }

  }

}
