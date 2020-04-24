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

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * Validates JCR names.
 */
final class NameUtil {

  // list of chars that are invalid for JCR names (from org.apache.jackrabbit.util.Text)
  private static final String ILLEGAL_CHARS = "%/[]*|\t\r\n";

  // we allow a single colon (:) as it separates the namespace
  private static final char NAMESPACE_SEPARATOR = ':';

  private static final Pattern ILLEGAL_CHARS_PATTERN;
  static {
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    for (int i = 0; i < ILLEGAL_CHARS.length(); i++) {
      if (i > 0) {
        sb.append("|");
      }
      sb.append(Pattern.quote(ILLEGAL_CHARS.substring(i, i + 1)));
    }
    sb.append(")");
    ILLEGAL_CHARS_PATTERN = Pattern.compile(sb.toString());
  }

  private NameUtil() {
    // static methods only
  }

  /**
   * Checks if the name is a valid JCR name.
   * @param name Name
   * @return true if valid
   */
  public static boolean isValidName(@Nullable String name) {
    if (StringUtils.isEmpty(name)) {
      return false;
    }
    int numberOfColons = StringUtils.countMatches(name, NAMESPACE_SEPARATOR);
    if (numberOfColons > 1) {
      return false;
    }
    return !ILLEGAL_CHARS_PATTERN.matcher(name).find();
  }

  /**
   * Ensures that all parts of the path are valid JCR names.
   * @param path Path.
   */
  public static void ensureValidPath(String path) {
    String relativePath;
    if (StringUtils.startsWith(path, "/")) {
      relativePath = path.substring(1);
    }
    else {
      relativePath = path;
    }
    String[] pathParts = StringUtils.split(relativePath, "/");
    for (String pathPart : pathParts) {
      if (!NameUtil.isValidName(pathPart)) {
        throw new IllegalArgumentException("Path contains illegal node names: " + path);
      }
    }
  }

}
