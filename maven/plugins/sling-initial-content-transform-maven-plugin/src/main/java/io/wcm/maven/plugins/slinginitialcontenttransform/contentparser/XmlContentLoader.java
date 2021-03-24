/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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
package io.wcm.maven.plugins.slinginitialcontenttransform.contentparser;

import static io.wcm.maven.plugins.slinginitialcontenttransform.contentparser.JsonContentLoader.IGNORED_NAMES;

import java.io.IOException;
import java.io.InputStream;

import org.apache.sling.contentparser.api.ContentParser;
import org.apache.sling.contentparser.api.ParserOptions;
import org.apache.sling.contentparser.xml.internal.XMLContentParser;

import io.wcm.tooling.commons.contentpackagebuilder.element.ContentElement;

/**
 * Reads XML (not FileVault) content data to be included in content package.
 */
public final class XmlContentLoader {

  private static final ContentParser XML_PARSER = new XMLContentParser();
  private static final ParserOptions XML_PARSER_OPTIONS = new ParserOptions()
      .detectCalendarValues(true)
      .ignorePropertyNames(IGNORED_NAMES)
      .ignoreResourceNames(IGNORED_NAMES);

  /**
   * Loads a JSON content and transform the contained data structured in nested maps, as supported by the
   * {@link io.wcm.tooling.commons.contentpackagebuilder.ContentPackageBuilder}.
   * @param inputStream JSON input stream
   * @return Nested map with content data
   * @throws IOException I/O exception
   */
  public ContentElement load(InputStream inputStream) throws IOException {
    ContentElementHandler contentHandler = new ContentElementHandler();
    XML_PARSER.parse(contentHandler, inputStream, XML_PARSER_OPTIONS);
    return contentHandler.getRoot();
  }

}
