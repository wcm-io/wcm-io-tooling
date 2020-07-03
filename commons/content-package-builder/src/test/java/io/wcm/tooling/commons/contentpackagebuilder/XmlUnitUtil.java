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
package io.wcm.tooling.commons.contentpackagebuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlunit.xpath.JAXPXPathEngine;
import org.xmlunit.xpath.XPathEngine;

import com.google.common.collect.ImmutableMap;

public final class XmlUnitUtil {

  public static final String CUSTOM_NS_PREFIX = "myns";
  public static final String CUSTOM_NS_URI = "http://myns";

  private static final XPathEngine XPATH_ENGINE = new JAXPXPathEngine();
  static {
    Map<String, String> namespaces = ImmutableMap.<String, String>builder()
        .putAll(XmlNamespaces.DEFAULT_NAMESPACES)
        .put(CUSTOM_NS_PREFIX, CUSTOM_NS_URI)
        .build();
    XPATH_ENGINE.setNamespaceContext(namespaces);
  }

  private XmlUnitUtil() {
    // static methods only
  }

  public static void assertXpathEvaluatesTo(String expected, String xpath, Node node) {
    assertEquals(expected, XPATH_ENGINE.evaluate(xpath, node));
  }

  public static void assertXpathEvaluatesTo(String expected, String xpath, Document doc) {
    assertXpathEvaluatesTo(expected, xpath, doc.getDocumentElement());
  }

  public static void assertXpathExists(String xpath, Node node) {
    assertTrue(XPATH_ENGINE.selectNodes(xpath, node).iterator().hasNext(), "XPath '" + xpath + "' exists");
  }

  public static void assertXpathExists(String xpath, Document doc) {
    assertXpathExists(xpath, doc.getDocumentElement());
  }

  public static void assertXpathNotExists(String xpath, Node node) {
    assertFalse(XPATH_ENGINE.selectNodes(xpath, node).iterator().hasNext(), "XPath '" + xpath + "' does not exist");
  }

  public static void assertXpathNotExists(String xpath, Document doc) {
    assertXpathNotExists(xpath, doc.getDocumentElement());
  }

}
