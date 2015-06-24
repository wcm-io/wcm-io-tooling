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

import static io.wcm.tooling.commons.contentpackagebuilder.XmlNamespaces.NS_JCR;

import java.text.DateFormat;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Builds CMS content packages.
 */
class XmlContentBuilder {

  private final DocumentBuilderFactory documentBuilderFactory;
  private final DocumentBuilder documentBuilder;
  private final Map<String, String> xmlNamespaces;
  private final ValueConverter valueConverter = new ValueConverter();

  static final String PN_PRIMARY_TYPE = "jcr:primaryType";
  static final String NT_PAGE = "cq:Page";
  static final String NT_PAGE_CONTENT = "cq:PageContent";
  static final String NT_UNSTRUCTURED = "nt:unstructured";
  static final String NT_FILE = "nt:file";
  static final String NT_RESOURCE = "nt:resource";

  public XmlContentBuilder(Map<String, String> xmlNamespaces) {
    this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
    this.documentBuilderFactory.setNamespaceAware(true);
    try {
      this.documentBuilder = documentBuilderFactory.newDocumentBuilder();
    }
    catch (ParserConfigurationException ex) {
      throw new RuntimeException("Failed to set up XML document builder: " + ex.getMessage(), ex);
    }
    this.xmlNamespaces = xmlNamespaces;
  }

  /**
   * Build XML for cq:Page.
   * @param content Content with page properties and nested nodes
   * @return cq:Page JCR XML
   */
  public Document buildPage(Map<String, Object> content) {
    Document doc = documentBuilder.newDocument();
    Element jcrRoot = createJcrRoot(doc, NT_PAGE);
    Element jcrContent = createJcrContent(doc, jcrRoot, NT_PAGE_CONTENT);

    exportPayload(doc, jcrContent, content);

    return doc;
  }

  /**
   * Build XML for any JCR content.
   * @param content Content with properties and nested nodes
   * @return JCR XML
   */
  public Document buildContent(Map<String, Object> content) {
    Document doc = documentBuilder.newDocument();

    String primaryType = StringUtils.defaultString((String)content.get(PN_PRIMARY_TYPE), NT_UNSTRUCTURED);
    Element jcrRoot = createJcrRoot(doc, primaryType);

    exportPayload(doc, jcrRoot, content);

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
    Element jcrRoot = createJcrRoot(doc, NT_FILE);
    Element jcrContent = createJcrContent(doc, jcrRoot, NT_RESOURCE);

    if (StringUtils.isNotEmpty(mimeType)) {
      setAttributeNamespaceAware(jcrContent, "jcr:mimeType", mimeType);
    }
    if (StringUtils.isNotEmpty(encoding)) {
      setAttributeNamespaceAware(jcrContent, "jcr:encoding", encoding);
    }

    return doc;
  }

  /**
   * Build filter XML for package metadata files.
   * @param filters Filters
   * @return Filter XML
   */
  public Document buildFilter(List<PackageFilter> filters) {
    Document doc = documentBuilder.newDocument();

    Element workspaceFilterElement = doc.createElement("workspaceFilter");
    workspaceFilterElement.setAttribute("version", "1.0");
    doc.appendChild(workspaceFilterElement);

    for (PackageFilter filter : filters) {
      Element filterElement = doc.createElement("filter");
      filterElement.setAttribute("root", filter.getRootPath());
      workspaceFilterElement.appendChild(filterElement);

      for (PackageFilterRule rule : filter.getRules()) {
        Element ruleElement = doc.createElement(rule.isInclude() ? "include" : "exclude");
        ruleElement.setAttribute("pattern", rule.getPattern());
        filterElement.appendChild(ruleElement);
      }
    }

    return doc;
  }

  private Element createJcrRoot(Document doc, String primaryType) {
    Element jcrRoot = doc.createElementNS(NS_JCR, "jcr:root");
    for (Map.Entry<String, String> namespace : xmlNamespaces.entrySet()) {
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
          setAttributeNamespaceAware(subElement, PN_PRIMARY_TYPE, NT_UNSTRUCTURED);
        }
        element.appendChild(subElement);
        exportPayload(doc, subElement, childMap);
      }
      else if (!hasAttributeNamespaceAware(element, entry.getKey())) {
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
    return xmlNamespaces.get(nsPrefix);
  }

  /**
   * @return Date format used for formatting timestamps in content package.
   */
  public DateFormat getJcrTimestampFormat() {
    return valueConverter.getJcrTimestampFormat();
  }

}
