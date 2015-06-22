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

import java.io.IOException;
import java.text.DateFormat;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.ImmutableMap;

/**
 * Builds CMS content packages.
 */
class PageXmlBuilder {

  private final DocumentBuilderFactory documentBuilderFactory;
  private final ValueConverter valueConverter = new ValueConverter();

  private static final String NS_JCR = "http://www.jcp.org/jcr/1.0";
  private static final String NS_CQ = "http://www.day.com/jcr/cq/1.0";
  private static final String NS_SLING = "http://sling.apache.org/jcr/sling/1.0";

  static final Map<String, String> BUILTIN_NAMESPACES = ImmutableMap.<String, String>builder()
      .put("jcr", NS_JCR)
      .put("cq", NS_CQ)
      .put("sling", NS_SLING)
      .build();

  static final String PN_PRIMARY_TYPE = "jcr:primaryType";

  public PageXmlBuilder() {
    this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
    this.documentBuilderFactory.setNamespaceAware(true);
  }

  public Document build(Map<String, Object> content) throws IOException {
    try {
      DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
      Document doc = docBuilder.newDocument();

      Element pageElement = doc.createElementNS(NS_JCR, "jcr:root");
      for (Map.Entry<String, String> namespace : BUILTIN_NAMESPACES.entrySet()) {
        pageElement.setAttribute("xmlns:" + namespace.getKey(), namespace.getValue());
      }

      pageElement.setAttributeNS(NS_JCR, PN_PRIMARY_TYPE, "cq:Page");
      doc.appendChild(pageElement);

      Element contentElement = doc.createElementNS(NS_JCR, "jcr:content");
      contentElement.setAttributeNS(NS_JCR, PN_PRIMARY_TYPE, "cq:PageContent");
      pageElement.appendChild(contentElement);

      exportPayload(doc, contentElement, content);

      return doc;
    }
    catch (ParserConfigurationException ex) {
      throw new IOException("Faild to generate XML: " + ex.getMessage(), ex);
    }
  }

  @SuppressWarnings("unchecked")
  private void exportPayload(Document doc, Element element, Map<String, Object> content) {
    for (Map.Entry<String,Object> entry : content.entrySet()) {
      Object value = entry.getValue();
      if (value == null) {
        continue;
      }
      if (value instanceof Map) {
        Map<String, Object> childMap = (Map<String, Object>)value;
        Element subElement = doc.createElement(entry.getKey());
        if (!childMap.containsKey(PN_PRIMARY_TYPE)) {
          subElement.setAttributeNS(NS_JCR, PN_PRIMARY_TYPE, "nt:unstructured");
        }
        element.appendChild(subElement);
        exportPayload(doc, subElement, childMap);
      }
      else {
        String stringValue = valueConverter.toString(value);
        String namespace = getNamespace(entry.getKey());
        if (namespace == null) {
          element.setAttribute(entry.getKey(), stringValue);
        }
        else {
          element.setAttributeNS(namespace, entry.getKey(), stringValue);
        }
      }
    }
  }

  private String getNamespace(String key) {
    for (Map.Entry<String, String> namespace : BUILTIN_NAMESPACES.entrySet()) {
      if (key.startsWith(namespace.getKey() + ":")) {
        return namespace.getValue();
      }
    }
    return null;
  }

  /**
   * @return Date format used for formatting timestamps in content package.
   */
  public DateFormat getJcrTimestampFormat() {
    return valueConverter.getJcrTimestampFormat();
  }

}
