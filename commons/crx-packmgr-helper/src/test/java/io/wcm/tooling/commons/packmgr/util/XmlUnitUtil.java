/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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
package io.wcm.tooling.commons.packmgr.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xmlunit.xpath.JAXPXPathEngine;
import org.xmlunit.xpath.XPathEngine;

import io.wcm.tooling.commons.contentpackagebuilder.XmlNamespaces;

public final class XmlUnitUtil {

  private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
  private static final XPathEngine XPATH_ENGINE = new JAXPXPathEngine();
  static {
    DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);
    XPATH_ENGINE.setNamespaceContext(XmlNamespaces.DEFAULT_NAMESPACES);
  }

  private XmlUnitUtil() {
    // static methods only
  }

  public static Document getXml(File file) {
    try {
      if (!file.exists()) {
        fail("File does not exist: " + file.getCanonicalPath());
      }
      DocumentBuilder documentBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
      return documentBuilder.parse(file);
    }
    catch (IOException | ParserConfigurationException | SAXException ex) {
      throw new RuntimeException("Unable to get XML file.", ex);
    }
  }

  public static void assertXpathEvaluatesTo(String expected, String xpath, Node node) {
    assertEquals(expected, XPATH_ENGINE.evaluate(xpath, node));
  }

  public static void assertXpathEvaluatesTo(String expected, String xpath, Document doc) {
    assertXpathEvaluatesTo(expected, xpath, doc.getDocumentElement());
  }

  public static void assertXpathEvaluatesTo(String expected, String xpath, File file) {
    assertXpathEvaluatesTo(expected, xpath, getXml(file));
  }

  public static void assertXpathExists(String xpath, Node node) {
    assertTrue(XPATH_ENGINE.selectNodes(xpath, node).iterator().hasNext(), "XPath '" + xpath + "' exists");
  }

  public static void assertXpathExists(String xpath, Document doc) {
    assertXpathExists(xpath, doc.getDocumentElement());
  }

  public static void assertXpathExists(String xpath, File file) {
    assertXpathExists(xpath, getXml(file));
  }

  public static void assertXpathNotExists(String xpath, Node node) {
    assertFalse(XPATH_ENGINE.selectNodes(xpath, node).iterator().hasNext(), "XPath '" + xpath + "' does not exist");
  }

  public static void assertXpathNotExists(String xpath, Document doc) {
    assertXpathNotExists(xpath, doc.getDocumentElement());
  }

  public static void assertXpathNotExists(String xpath, File file) {
    assertXpathNotExists(xpath, getXml(file));
  }

}
