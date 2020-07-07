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
package io.wcm.tooling.commons.contentpackagebuilder;

import java.util.HashMap;
import java.util.Map;

import io.wcm.tooling.commons.contentpackagebuilder.element.ContentElement;

/**
 * Converts a structure of {@link ContentElement} objects to nested maps.
 */
final class ContentElementConverter {

  private ContentElementConverter() {
    // static methods only
  }

  static Map<String, Object> toMap(ContentElement content) {
    Map<String, Object> result = new HashMap<>();
    result.putAll(content.getProperties());
    for (Map.Entry<String, ContentElement> entry : content.getChildren().entrySet()) {
      result.put(entry.getKey(), toMap(entry.getValue()));
    }
    return result;
  }

}
