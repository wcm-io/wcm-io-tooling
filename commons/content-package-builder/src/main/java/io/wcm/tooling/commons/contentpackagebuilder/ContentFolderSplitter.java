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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Splits an arbitrary content fragment into separate chunks for each node type that requires it's own folder.
 */
final class ContentFolderSplitter {

  private ContentFolderSplitter() {
    // static methods only
  }

  public static List<ContentPart> split(Map<String, Object> content) {
    List<ContentPart> result = new ArrayList<>();
    collectRecursive("", content, result);
    return result;
  }

  private static void collectRecursive(String path, Map<String, Object> content, List<ContentPart> result) {
    Map<String, Object> resultContent = new HashMap<>();
    result.add(new ContentPart(path, resultContent));
    for (Map.Entry<String, Object> entry : content.entrySet()) {
      if (entry.getValue() instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> childMap = (Map<String, Object>)entry.getValue();
        if (NodeTypes.hasFolderNodeType(childMap)) {
          collectRecursive(path + "/" + entry.getKey(), childMap, result);
          continue;
        }
      }
      resultContent.put(entry.getKey(), entry.getValue());
    }
  }

  static class ContentPart {

    private final String path;
    private final Map<String, Object> content;

    ContentPart(String path, Map<String, Object> content) {
      this.path = path;
      this.content = content;
    }

    public String getPath() {
      return this.path;
    }

    public Map<String, Object> getContent() {
      return this.content;
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }

  }

}
