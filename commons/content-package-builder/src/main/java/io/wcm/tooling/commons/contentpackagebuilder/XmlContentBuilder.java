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

import java.text.DateFormat;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.ImmutableMap;

/**
 * Builds CMS content packages.
 */
class XmlContentBuilder {

  private final DocumentBuilderFactory documentBuilderFactory;
  private final DocumentBuilder documentBuilder;
  private final ValueConverter valueConverter = new ValueConverter();

  private static final String NS_JCR = "http://www.jcp.org/jcr/1.0";
  private static final String NS_JCR_NT = "http://www.jcp.org/jcr/nt/1.0";
  private static final String NS_CQ = "http://www.day.com/jcr/cq/1.0";
  private static final String NS_SLING = "http://sling.apache.org/jcr/sling/1.0";

  static final Map<String, String> BUILTIN_NAMESPACES = ImmutableMap.<String, String>builder()
      .put("jcr", NS_JCR)
      .put("nt", NS_JCR_NT)
      .put("cq", NS_CQ)
      .put("sling", NS_SLING)
      .build();

  static final String PN_PRIMARY_TYPE = "jcr:primaryType";

  public XmlContentBuilder() {
    this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
    this.documentBuilderFactory.setNamespaceAware(true);
    try {
      this.documentBuilder = documentBuilderFactory.newDocumentBuilder();
    }
    catch (ParserConfigurationException ex) {
      throw new RuntimeException("Failed to set up XML document builder: " + ex.getMessage(), ex);
    }
  }

  /**
   * Build XML for cq:Page.
   * @param content Content wiht page properties and nested nodes
   * @return cq:Page XML
   */
  public Document buildPage(Map<String, Object> content) {
    Document doc = documentBuilder.newDocument();
    Element jcrRoot = createJcrRoot(doc, "cq:Page");
    Element jcrContent = createJcrContent(doc, jcrRoot, "cq:PageContent");

    exportPayload(doc, jcrContent, content);

    return doc;
  }

  /**
   * Build XML for nt:file
   * @param mimeType Mime type
   * @param encoding Encoding
   * @return nt:file XML
   */
  public Document buildNtFile(String mimeType, String encoding) {
    Document doc = documentBuilder.newDocument();
    Element jcrRoot = createJcrRoot(doc, "nt:file");
    Element jcrContent = createJcrContent(doc, jcrRoot, "nt:resource");

    if (StringUtils.isNotEmpty(mimeType)) {
      setAttributeNamespaceAware(jcrContent, "jcr:mimeType", mimeType);
    }
    if (StringUtils.isNotEmpty(encoding)) {
      setAttributeNamespaceAware(jcrContent, "jcr:encoding", encoding);
    }

    return doc;
  }

  private Element createJcrRoot(Document doc, String primaryType) {
    Element jcrRoot = doc.createElementNS(NS_JCR, "jcr:root");
    for (Map.Entry<String, String> namespace : BUILTIN_NAMESPACES.entrySet()) {
      jcrRoot.setAttribute("xmlns:" + namespace.getKey(), namespace.getValue());
    }
    setAttributeNamespaceAware(jcrRoot, PN_PRIMARY_TYPE, primaryType);
    doc.appendChild(jcrRoot);
    return jcrRoot;
  }

  private Element createJcrContent(Document doc, Element jcrRoot, String primaryType) {
    Element jcrContent = doc.createElementNS(NS_JCR, "jcr:content");
    setAttributeNamespaceAware(jcrContent, PN_PRIMARY_TYPE, primaryType);
    jcrRoot.appendChild(jcrContent);
    return jcrContent;
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
        if (!hasAttributeNamespaceAware(subElement, PN_PRIMARY_TYPE) && !childMap.containsKey(PN_PRIMARY_TYPE)) {
          setAttributeNamespaceAware(subElement, PN_PRIMARY_TYPE, "nt:unstructured");
        }
        element.appendChild(subElement);
        exportPayload(doc, subElement, childMap);
      }
      else {
        String stringValue = valueConverter.toString(value);
        setAttributeNamespaceAware(element, entry.getKey(), stringValue);
      }
    }
  }

  private void setAttributeNamespaceAware(Element element, String key, String value) {
    String namespace = getNamespace(key);
    if (namespace == null) {
      element.setAttribute(key, value);
    }
    else {
      element.setAttributeNS(namespace, key, value);
    }
  }

  private boolean hasAttributeNamespaceAware(Element element, String key) {
    String namespace = getNamespace(key);
    if (namespace == null) {
      return element.hasAttribute(key);
    }
    else {
      return element.hasAttributeNS(namespace, key);
    }
  }

  private String getNamespace(String key) {
    if (!StringUtils.contains(key, ":")) {
      return null;
    }
    String nsPrefix = StringUtils.substringBefore(key, ":");
    return BUILTIN_NAMESPACES.get(nsPrefix);
  }

  /**
   * @return Date format used for formatting timestamps in content package.
   */
  public DateFormat getJcrTimestampFormat() {
    return valueConverter.getJcrTimestampFormat();
  }

}
