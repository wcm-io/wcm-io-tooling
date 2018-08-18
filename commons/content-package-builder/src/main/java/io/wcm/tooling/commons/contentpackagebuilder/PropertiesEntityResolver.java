/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2018 wcm.io
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
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

class PropertiesEntityResolver implements EntityResolver {

  private static final String PROPERTIES_DTD = "http://java.sun.com/dtd/properties.dtd";

  @Override
  public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
    if (StringUtils.equals(systemId, PROPERTIES_DTD)) {
      InputStream is = getClass().getResourceAsStream("/entities/properties.dtd");
      return new InputSource(is);
    }
    return null;
  }

}
