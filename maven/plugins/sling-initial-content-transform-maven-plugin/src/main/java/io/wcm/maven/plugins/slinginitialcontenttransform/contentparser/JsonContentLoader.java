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

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.Set;

import org.apache.jackrabbit.JcrConstants;
import org.apache.sling.contentparser.api.ContentParser;
import org.apache.sling.contentparser.api.ParserOptions;
import org.apache.sling.contentparser.json.JSONParserFeature;
import org.apache.sling.contentparser.json.JSONParserOptions;
import org.apache.sling.contentparser.json.internal.JSONContentParser;

import com.google.common.collect.ImmutableSet;

import io.wcm.tooling.commons.contentpackagebuilder.element.ContentElement;

/**
 * Reads JSON content data to be included in content package.
 */
public final class JsonContentLoader {

  static final Set<String> IGNORED_NAMES = ImmutableSet.of(
      JcrConstants.JCR_BASEVERSION,
      JcrConstants.JCR_PREDECESSORS,
      JcrConstants.JCR_SUCCESSORS,
      JcrConstants.JCR_CREATED,
      JcrConstants.JCR_VERSIONHISTORY,
      "jcr:checkedOut",
      "jcr:isCheckedOut",
      ":jcr:data");

  private static final ContentParser JSON_PARSER = new JSONContentParser();
  private static final ParserOptions JSON_PARSER_OPTIONS = new JSONParserOptions()
      .withFeatures(EnumSet.of(JSONParserFeature.COMMENTS, JSONParserFeature.QUOTE_TICK))
      .detectCalendarValues(true)
      .ignorePropertyNames(IGNORED_NAMES)
      .ignoreResourceNames(IGNORED_NAMES);

  /**
   * Loads a JSON content and transforms the contained data structured in nested maps, as supported by the
   * {@link io.wcm.tooling.commons.contentpackagebuilder.ContentPackageBuilder}.
   * @param inputStream JSON input stream
   * @return Nested map with content data
   * @throws IOException I/O exception
   */
  public ContentElement load(InputStream inputStream) throws IOException {
    ContentElementHandler contentHandler = new ContentElementHandler();
    JSON_PARSER.parse(contentHandler, inputStream, JSON_PARSER_OPTIONS);
    return contentHandler.getRoot();
  }

}
