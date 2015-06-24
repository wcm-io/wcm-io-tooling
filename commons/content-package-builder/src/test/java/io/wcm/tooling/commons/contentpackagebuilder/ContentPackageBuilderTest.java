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

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathExists;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.CharEncoding;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.zeroturnaround.zip.ZipUtil;

import com.google.common.collect.ImmutableMap;

public class ContentPackageBuilderTest {

  private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
  static {
    DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);
  }

  private ContentPackageBuilder underTest;
  private File testFile;

  @Before
  public void setUp() {
    XmlUnitUtil.registerXmlUnitNamespaces();
    underTest = new ContentPackageBuilder();

    testFile = new File("target/testing/" + UUID.randomUUID() + ".zip");
    testFile.getParentFile().mkdirs();
  }

  @After
  public void tearDown() {
    if (testFile.exists()) {
      testFile.delete();
    }
  }

  @Test
  public void testMetadata() throws Exception {
    underTest.name("myName");
    underTest.group("myGroup");
    underTest.description("myDescription");
    underTest.createdBy("myUser");
    underTest.version("1.2.3");
    underTest.rootPath("/content/mypath");

    try (ContentPackage contentPackage = underTest.build(testFile)) {
      // just build empty content package to test meta data
      assertEquals("/content/mypath", contentPackage.getRootPath());
    }

    // validate metadata files
    Document configXml = getXmlFromZip("META-INF/vault/config.xml");
    assertXpathEvaluatesTo("1.1", "/vaultfs/@version", configXml);

    Document filterXml = getXmlFromZip("META-INF/vault/filter.xml");
    assertXpathEvaluatesTo("1", "count(/workspaceFilter/filter)", filterXml);
    assertXpathEvaluatesTo("/content/mypath", "/workspaceFilter/filter[1]/@root", filterXml);

    Document propsXml = getXmlFromZip("META-INF/vault/properties.xml");
    assertXpathEvaluatesTo("myGroup", "/properties/entry[@key='group']", propsXml);
    assertXpathEvaluatesTo("myName", "/properties/entry[@key='name']", propsXml);
    assertXpathEvaluatesTo("myDescription", "/properties/entry[@key='description']", propsXml);
    assertXpathEvaluatesTo("/etc/packages/myGroup/myName.zip", "/properties/entry[@key='path']", propsXml);
    assertXpathExists("/properties/entry[@key='created']", propsXml);
    assertXpathEvaluatesTo("myUser", "/properties/entry[@key='createdBy']", propsXml);
    assertXpathEvaluatesTo("1.2.3", "/properties/entry[@key='version']", propsXml);
    assertXpathEvaluatesTo("false", "/properties/entry[@key='requiresRoot']", propsXml);

    Document settingsXml = getXmlFromZip("META-INF/vault/settings.xml");
    assertXpathEvaluatesTo("1.0", "/vault/@version", settingsXml);

  }

  @Test
  public void testAddPages() throws Exception {

    ContentPackageBuilder builder = underTest.group("myGroup").name("myName").rootPath("/test");
    try (ContentPackage contentPackage = builder.build(testFile)) {
      // add two content pages
      contentPackage.addPage("/content/page1", ImmutableMap.<String, Object>of("var1", "v1"));
      contentPackage.addPage("/content/page2", ImmutableMap.<String, Object>of("var2", "v2"));
    }

    // validate resulting XML
    Document page1Xml = getXmlFromZip("jcr_root/content/page1/.content.xml");
    assertXpathEvaluatesTo("v1", "/jcr:root/jcr:content/@var1", page1Xml);

    Document page2Xml = getXmlFromZip("jcr_root/content/page2/.content.xml");
    assertXpathEvaluatesTo("v2", "/jcr:root/jcr:content/@var2", page2Xml);
  }

  @Test
  public void testAddContentMultipleFilters() throws Exception {

    ContentPackageBuilder builder = underTest.group("myGroup").name("myName")
        .filter(new PackageFilter("/path1"))
        .filter(new PackageFilter("/path2"));
    try (ContentPackage contentPackage = builder.build(testFile)) {
      // add some content
      contentPackage.addContent("/content/node1", ImmutableMap.<String, Object>of("var1", "v1"));
      assertEquals(2, contentPackage.getFilters().size());
    }

    // validate resulting XML
    Document filterXml = getXmlFromZip("META-INF/vault/filter.xml");
    assertXpathEvaluatesTo("2", "count(/workspaceFilter/filter)", filterXml);
    assertXpathEvaluatesTo("/path1", "/workspaceFilter/filter[1]/@root", filterXml);
    assertXpathEvaluatesTo("/path2", "/workspaceFilter/filter[2]/@root", filterXml);
  }

  @Test
  public void testAddContent() throws Exception {

    ContentPackageBuilder builder = underTest.group("myGroup").name("myName").rootPath("/test");
    try (ContentPackage contentPackage = builder.build(testFile)) {
      // add some content
      contentPackage.addContent("/content/node1", ImmutableMap.<String, Object>of("var1", "v1"));
      contentPackage.addContent("/content/node2", ImmutableMap.<String, Object>of("var2", "v2"));
    }

    // validate resulting XML
    Document page1Xml = getXmlFromZip("jcr_root/content/node1/.content.xml");
    assertXpathEvaluatesTo("v1", "/jcr:root/@var1", page1Xml);

    Document page2Xml = getXmlFromZip("jcr_root/content/node2/.content.xml");
    assertXpathEvaluatesTo("v2", "/jcr:root/@var2", page2Xml);
  }

  @Test
  public void testAddContentCustomNamespace() throws Exception {

    ContentPackageBuilder builder = underTest.group("myGroup").name("myName").rootPath("/test")
        .xmlNamespace(XmlUnitUtil.CUSTOM_NS_PREFIX, XmlUnitUtil.CUSTOM_NS_URI);
    try (ContentPackage contentPackage = builder.build(testFile)) {
      // add some content
      contentPackage.addContent("/content/node1", ImmutableMap.<String, Object>of("myns:var1", "v1"));
    }

    // validate resulting XML
    Document page1Xml = getXmlFromZip("jcr_root/content/node1/.content.xml");
    assertXpathEvaluatesTo("v1", "/jcr:root/@myns:var1", page1Xml);
  }

  @Test
  public void testAddBinaries() throws Exception {

    byte[] data1 = "content1".getBytes(CharEncoding.UTF_8);
    byte[] data2 = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05 };

    ContentPackageBuilder builder = underTest.group("myGroup").name("myName").rootPath("/test");
    try (ContentPackage contentPackage = builder.build(testFile)) {

      // add two binary files
      try (InputStream is1 = new ByteArrayInputStream(data1)) {
        contentPackage.addFile("/content/file1.txt", is1, "text/plain;charset=UTF-8");
      }
      try (InputStream is2 = new ByteArrayInputStream(data2)) {
        contentPackage.addFile("/content/file2.bin", is2);
      }

    }

    // validate resulting files
    assertArrayEquals(data1, getDataFromZip("jcr_root/content/file1.txt"));
    Document metaXml = getXmlFromZip("jcr_root/content/file1.txt.dir/.content.xml");
    assertXpathEvaluatesTo("text/plain", "/jcr:root/jcr:content/@jcr:mimeType", metaXml);
    assertXpathEvaluatesTo("UTF-8", "/jcr:root/jcr:content/@jcr:encoding", metaXml);

    assertArrayEquals(data2, getDataFromZip("jcr_root/content/file2.bin"));
  }

  private byte[] getDataFromZip(String path) throws Exception {
    byte[] data = ZipUtil.unpackEntry(testFile, path);
    if (data == null) {
      throw new FileNotFoundException("File not found in ZIP: " + path);
    }
    return data;
  }

  private Document getXmlFromZip(String path) throws Exception {
    byte[] data = getDataFromZip(path);
    DocumentBuilder documentBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
    return documentBuilder.parse(new ByteArrayInputStream(data));
  }

}
