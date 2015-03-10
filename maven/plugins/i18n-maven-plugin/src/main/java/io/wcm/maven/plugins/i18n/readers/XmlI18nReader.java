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
package io.wcm.maven.plugins.i18n.readers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 * Reads i18n resources from XML files.
 */
public class XmlI18nReader implements I18nReader {

  @Override
  public Map<String, String> read(File sourceFile) throws IOException {
    try {
      SAXBuilder builder = new SAXBuilder();
      Document doc = builder.build(sourceFile);
      Map<String, String> map = new HashMap<String, String>();
      parseXml(doc.getRootElement(), map, "");
      return map;
    }
    catch (JDOMException ex) {
      throw new IOException("Unable to read XML from " + sourceFile.getAbsolutePath(), ex);
    }
  }

  private void parseXml(Element node, Map<String, String> map, String prefix) {
    List<Element> children = node.getChildren();
    for (Element child : children) {
      String key = child.getName();
      if (child.getChildren().size() > 0) {
        parseXml(child, map, prefix + key + ".");
      }
      else {
        map.put(prefix + key, child.getText());
      }
    }
  }

}
